package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import group.msg.jpowermonitor.util.DataPointUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static group.msg.jpowermonitor.agent.MeasurePower.getCurrentCpuPowerInWatts;
import static group.msg.jpowermonitor.agent.Utils.MATH_CONTEXT;
import static group.msg.jpowermonitor.agent.Utils.UNIT_JOULE;
import static group.msg.jpowermonitor.agent.Utils.UNIT_WATTS;

@Slf4j
public class PowerStatistics extends TimerTask {

    private static final String CLASS_METHOD_SEPARATOR = ".";

    private final AtomicReference<DataPoint> energyConsumptionTotalInJoule = new AtomicReference<>(new DataPoint("energyConsumptionTotalInJoule", BigDecimal.ZERO, UNIT_JOULE, LocalDateTime.now()));
    private final Map<Long, Long> threadsCpuTime = new HashMap<>();
    private final List<Activity> recentEnergyConsumption = Collections.synchronizedList(new LinkedList<>());


    private final long measurementInterval;
    private final long gatherStatisticsInterval;
    private final BigDecimal activityToEnergyRatio;
    private final long pid;
    private final ThreadMXBean threadMXBean;
    private static Set<String> packageFilter;

    public PowerStatistics(long measurementInterval, long gatherStatisticsInterval, long pid, ThreadMXBean threadMXBean, Set<String> packageFilter) {
        this.measurementInterval = measurementInterval;
        this.gatherStatisticsInterval = gatherStatisticsInterval;
        this.activityToEnergyRatio = BigDecimal.valueOf(((double) this.gatherStatisticsInterval) / ((double) this.measurementInterval));
        this.pid = pid;
        this.threadMXBean = threadMXBean;
        PowerStatistics.packageFilter = packageFilter;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(PowerStatistics.class.getSimpleName() + " Thread");
        log.trace("Start new gather statistics and power measurement cycle...");

        Map<Long, List<MethodActivity>> methodActivityPerThread = new HashMap<>();
        Set<Thread> threads = Thread.getAllStackTraces().keySet();

        int duration = 0;
        while (duration < measurementInterval) {
            gatherMethodActivityPerThread(methodActivityPerThread, threads);
            duration += gatherStatisticsInterval;
            // Sleep for statisticsInterval, e. g. 10 ms
            try {
                Thread.sleep(gatherStatisticsInterval);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }

        // Adds current power to total energy consumption of application
        // It's fine to treat power and energy as equal here because power is measured each second (1 joule = 1 watt / second)
        //TODO proper conversion
        DataPoint currentPower = getCurrentCpuPowerInWatts();
        DataPoint currentEnergy = DataPointUtil.cloneWithNewUnit(currentPower, UNIT_JOULE);
        energyConsumptionTotalInJoule.getAndAccumulate(currentEnergy, DataPointUtil::add);

        // CPU time for each thread
        long totalApplicationCpuTime = CpuAndThreadUtils.getTotalApplicationCpuTimeAndCalculateCpuTimePerApplicationThread(threadMXBean, threadsCpuTime, threads);
        Map<Long, BigDecimal> powerPerThread = CpuAndThreadUtils.calculatePowerPerApplicationThread(threadsCpuTime, currentPower, totalApplicationCpuTime);

        // Now we have power for each thread, and stats for methods in each thread
        // We allocated power to each method based on activity
        allocateEnergyUsageToActivity(methodActivityPerThread, powerPerThread, recentEnergyConsumption);
    }

    private static void gatherMethodActivityPerThread(Map<Long, List<MethodActivity>> methodActivityPerThread, Set<Thread> threads) {
        for (Thread thread : threads) {
            // Only consider threads that are currently running (not waiting or blocked)
            if (Thread.State.RUNNABLE == thread.getState()) {
                StackTraceElement[] stackTrace = thread.getStackTrace();
                if (stackTrace.length == 0) {
                    continue;
                }

                long threadId = thread.getId();
                methodActivityPerThread.putIfAbsent(threadId, new LinkedList<>());
                MethodActivity activity = new MethodActivity();

                Arrays.stream(stackTrace)
                    .findFirst()
                    .map(PowerStatistics::getFullQualifiedMethodName)
                    .ifPresent(activity::setMethodQualifier);
                Arrays.stream(stackTrace)
                    .map(PowerStatistics::getFullQualifiedMethodName)
                    .filter(PowerStatistics::isMethodInFilterList)
                    .findFirst()
                    .ifPresent(activity::setFilteredMethodQualifier);

                methodActivityPerThread.get(threadId).add(activity);
            }
        }
    }

    @NotNull
    private static String getFullQualifiedMethodName(StackTraceElement ste) {
        return ste.getClassName() + CLASS_METHOD_SEPARATOR + ste.getMethodName();
    }

    private static boolean isMethodInFilterList(String method) {
        return packageFilter.stream()
            .anyMatch(method::startsWith);
    }

    private void allocateEnergyUsageToActivity(Map<Long, List<MethodActivity>> methodActivityPerThread, Map<Long, BigDecimal> powerPerApplicationThread, List<Activity> energyConsumption) {
        for (Map.Entry<Long, List<MethodActivity>> entry : methodActivityPerThread.entrySet()) {
            long threadId = entry.getKey();

            for (MethodActivity activity : entry.getValue()) {
                Quantity methodPower = new Quantity(powerPerApplicationThread.get(threadId).multiply(activityToEnergyRatio), UNIT_WATTS);
                Quantity methodEnergy = new Quantity(methodPower.getValue().multiply(BigDecimal.valueOf(measurementInterval)).divide(BigDecimal.valueOf(1000L), MATH_CONTEXT), UNIT_JOULE);

                if (methodEnergy.getValue().signum() > 0) {
                    activity.setRepresentedQuantity(methodEnergy);
                    energyConsumption.add(activity);
                }
            }
        }
    }

    /**
     * @return total energy consumption of application
     */
    public AtomicReference<DataPoint> getEnergyConsumptionTotalInJoule() {
        return energyConsumptionTotalInJoule;
    }

    public List<Activity> getRecentActivity() {
        return recentEnergyConsumption.stream()
            .filter(Activity::isFinalized)
            .collect(Collectors.toList());
    }

    /**
     * @return process id
     */
    public long getPid() {
        return pid;
    }
}

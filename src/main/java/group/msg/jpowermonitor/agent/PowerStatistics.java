package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

@Slf4j
public class PowerStatistics extends TimerTask {

    private static final String CLASS_METHOD_SEPARATOR = ".";
    private static final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);
    private final AtomicReference<DataPoint> energyConsumptionTotalInJoule = new AtomicReference<>(new DataPoint("energyConsumptionTotalInJoule", BigDecimal.ZERO, Unit.JOULE, LocalDateTime.now()));
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
        DataPoint currentEnergy = cloneDataPointWithNewUnit(currentPower, Unit.JOULE);
        energyConsumptionTotalInJoule.getAndAccumulate(currentEnergy, this::addDataPoint);

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
                Quantity methodPower = new Quantity(powerPerApplicationThread.get(threadId).multiply(activityToEnergyRatio), Unit.WATT);
                Quantity methodEnergy = new Quantity(methodPower.getValue().multiply(BigDecimal.valueOf(measurementInterval)).divide(BigDecimal.valueOf(1000L), MATH_CONTEXT), Unit.JOULE);

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


    /**
     * Checks if two <code>DataPoint</code> instances are addable
     *
     * @param dp1 <code>DataPoint</code>
     * @param dp2 <code>DataPoint</code>
     * @return <code>true</code>, if addable
     */
    public boolean areDataPointsAddable(@NotNull DataPoint dp1, @NotNull DataPoint dp2) {
        log.trace("dp1 = {}, dp2 = {}", dp1, dp2);
        if (dp1.getUnit() == null || dp2.getUnit() == null
            || dp1.getValue() == null || dp2.getValue() == null
            || !dp1.getUnit().equals(dp2.getUnit())) {
            log.error("not addable: dp1 = {}, dp2 = {}", dp1, dp2);
            return false;
        }
        return true;
    }

    /**
     * Add the values of multiple <code>DataPoint</code> instances - if units match<br>
     * First <code>DataPoint</code> in <code>dataPoints</code> is reference for unit
     *
     * @param dataPoints <code>DataPoint</code> instances to sum up values
     * @return sum of all given <code>dataPoints</code> with same unit
     */
    public DataPoint addDataPoint(@NotNull DataPoint... dataPoints) {
        if (dataPoints == null || dataPoints.length < 2) {
            throw new IllegalArgumentException("dataPoints must contain at least two elements!");
        }
        DataPoint reference = dataPoints[0];
        AtomicReference<BigDecimal> sum = new AtomicReference<>(BigDecimal.ZERO);
        Arrays.stream(dataPoints).filter(dp -> areDataPointsAddable(reference, dp)).forEach(dp -> sum.getAndAccumulate(dp.getValue(), BigDecimal::add));
        return new DataPoint(reference.getName(), sum.get(), reference.getUnit(), LocalDateTime.now());
    }

    /**
     * Clones <code>DataPoint</code> but with new <code>unit</code>
     *
     * @param dp   <code>DataPoint</code> to clone
     * @param unit new unit
     * @return <code>DataPoint</code> with new <code>unit</code>
     */
    public DataPoint cloneDataPointWithNewUnit(@NotNull DataPoint dp, @NotNull Unit unit) {
        return new DataPoint(dp.getName(), dp.getValue(), unit, dp.getTime());
    }

}

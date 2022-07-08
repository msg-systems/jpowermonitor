package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static group.msg.jpowermonitor.agent.MeasurePower.getCurrentCpuPowerInWatts;

public class PowerStatistics extends TimerTask {
    private static final String CLASS_METHOD_SEPARATOR = ".";
    private static final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);
    private final AtomicReference<DataPoint> energyConsumptionTotalInJoule =
        new AtomicReference<>(new DataPoint("energyConsumptionTotalInJoule", BigDecimal.ZERO, Unit.JOULE, LocalDateTime.now(), null));
    private final Map<Long, Long> threadsCpuTime = new HashMap<>();
    private final Map<String, DataPoint> energyConsumptionPerMethod = new ConcurrentHashMap<>();
    private final long measurementInterval;
    private final long gatherStatisticsInterval;
    private final BigDecimal activityToEnergyRatio;
    private final long pid;
    private final ThreadMXBean threadMXBean;
    private static Set<String> packageFilter;

    public PowerStatistics(long measurementInterval, long gatherStatisticsInterval, long pid, ThreadMXBean threadMXBean, Set<String> packageFilter) {
        this.measurementInterval = measurementInterval;
        this.gatherStatisticsInterval = gatherStatisticsInterval;
        this.activityToEnergyRatio = measurementInterval > 0 ?
            new BigDecimal(this.gatherStatisticsInterval).divide(new BigDecimal(this.measurementInterval), MATH_CONTEXT) :
            BigDecimal.ZERO;
        this.pid = pid;
        this.threadMXBean = threadMXBean;
        PowerStatistics.packageFilter = packageFilter;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(PowerStatistics.class.getSimpleName() + " Thread");

        Map<Long, Set<MethodActivity>> methodActivityPerThread = new HashMap<>();
        Set<Thread> threads = Thread.getAllStackTraces().keySet();

        int duration = 0;
        while (duration < measurementInterval) {
            gatherMethodActivityPerThread(methodActivityPerThread, threads);
            duration += gatherStatisticsInterval;
            // Sleep for statisticsInterval, e. g. 10 ms
            try {
                TimeUnit.MILLISECONDS.sleep(gatherStatisticsInterval);
            } catch (InterruptedException ex) {
                System.err.println(ex.getLocalizedMessage());
                ex.printStackTrace();
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
        allocateEnergyUsageToActivity(methodActivityPerThread, powerPerThread);

        writePowerMeasurementsToCsvFiles(methodActivityPerThread);
    }

    private static void gatherMethodActivityPerThread(Map<Long, Set<MethodActivity>> methodActivityPerThread, Set<Thread> threads) {
        for (Thread thread : threads) {
            // Only consider threads that are currently running (not waiting or blocked)
            if (Thread.State.RUNNABLE == thread.getState()) {
                StackTraceElement[] stackTrace = thread.getStackTrace();
                if (stackTrace.length == 0) {
                    continue;
                }

                long threadId = thread.getId();
                methodActivityPerThread.putIfAbsent(threadId, new HashSet<>());
                MethodActivity activity = new MethodActivity();
                activity.setProcessID(threadId);
                activity.setTime(LocalDateTime.now());

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

    private void allocateEnergyUsageToActivity(Map<Long, Set<MethodActivity>> methodActivityPerThread, Map<Long, BigDecimal> powerPerApplicationThread) {
        for (Map.Entry<Long, Set<MethodActivity>> entry : methodActivityPerThread.entrySet()) {
            long threadId = entry.getKey();

            for (MethodActivity activity : entry.getValue()) {
                Quantity methodPower = Quantity.of(powerPerApplicationThread.get(threadId).multiply(activityToEnergyRatio), Unit.WATT);
                Quantity methodEnergy = Quantity.of(
                    methodPower.getValue().multiply(BigDecimal.valueOf(measurementInterval)).divide(BigDecimal.valueOf(1000L), MATH_CONTEXT),
                    Unit.JOULE
                );

                if (methodEnergy.getValue().signum() > 0) {
                    activity.setRepresentedQuantity(methodEnergy);
                    appendEnergyUsage(activity);
                    activity.setRepresentedQuantity(methodPower);
                }
            }
        }
    }

    private void appendEnergyUsage(MethodActivity activity) {
        if (!activity.isFinalized()) {
            return;
        }

        energyConsumptionPerMethod.merge(
            activity.getIdentifier(false),
            getDataPointFrom(activity, false),
            this::addDataPoint
        );
    }

    private void writePowerMeasurementsToCsvFiles(Map<Long, Set<MethodActivity>> methodActivityPerThread) {
        new ResultsWriter(this, false).createUnfilteredAndFilteredPowerConsumptionPerMethodCsvAndWriteToFiles(
            methodActivityPerThread.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
        );
    }

    /**
     * @return total energy consumption of application
     */
    public AtomicReference<DataPoint> getEnergyConsumptionTotalInJoule() {
        return energyConsumptionTotalInJoule;
    }

    public Map<String, DataPoint> getEnergyConsumptionPerMethod(boolean asFiltered) {
        return energyConsumptionPerMethod.entrySet().stream()
            .filter(e -> asFiltered ? isMethodInFilterList(e.getKey()) : e.getKey() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * @return process id
     */
    public long getPid() {
        return pid;
    }

    /**
     * Creates a new {@link DataPoint} equivalent for the {@link Activity} provided.
     *
     * @param activity {@link Activity} to create a {@link DataPoint} for
     * @param filtered if the {@link Activity}'s identifier should be filtered
     * @return new {@link DataPoint}
     */
    public DataPoint getDataPointFrom(Activity activity, boolean filtered) {
        Optional<Quantity> quantity = Optional.ofNullable(activity.getRepresentedQuantity());

        return new DataPoint(
            activity.getIdentifier(filtered),
            quantity.map(Quantity::getValue).orElse(null),
            quantity.map(Quantity::getUnit).orElse(null),
            activity.getTime(),
            activity.getProcessID()
        );
    }

    /**
     * Transforms a {@link Collection} of {@link Activity}s into a {@link Map} of {@link DataPoint}s
     * where each entry consists of
     * <ul>
     *     <li>
     *         key - the activity identifier
     *     </li>
     *     <li>
     *         value - a {@link DataPoint} consisting of the sum of all {@link Activity}s
     *         resp. their {@link Quantity} with the identifier in the collection provided
     *     </li>
     * </ul>
     *
     * @param activityCollection {@link Collection} of {@link Activity}s
     * @param filtered           if the filtered identifier should be used instead
     * @return aggregated {@link Map}
     */
    public Map<String, DataPoint> aggregateActivityToDataPoints(Collection<Activity> activityCollection, boolean filtered) {
        return activityCollection.stream()
            .filter(activity -> activity.getIdentifier(filtered) != null)
            .filter(Activity::isFinalized)
            .collect(Collectors.toMap(
                activity -> activity.getIdentifier(filtered),
                activity -> getDataPointFrom(activity, filtered),
                this::addDataPoint
            ));
    }

    /**
     * Checks if two <code>DataPoint</code> instances are addable
     *
     * @param dp1 <code>DataPoint</code>
     * @param dp2 <code>DataPoint</code>
     * @return <code>true</code>, if addable
     */
    public boolean areDataPointsAddable(@NotNull DataPoint dp1, @NotNull DataPoint dp2) {
        if (dp1.getUnit() == null || dp2.getUnit() == null
            || dp1.getValue() == null || dp2.getValue() == null
            || !dp1.getUnit().equals(dp2.getUnit())) {
            System.err.println("not addable: dp1 = " + dp1 + ", dp2 = " + dp2);
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
        return new DataPoint(reference.getName(), sum.get(), reference.getUnit(), LocalDateTime.now(), reference.getThreadId());
    }

    /**
     * Clones <code>DataPoint</code> but with new <code>unit</code>
     *
     * @param dp   <code>DataPoint</code> to clone
     * @param unit new unit
     * @return <code>DataPoint</code> with new <code>unit</code>
     */
    public DataPoint cloneDataPointWithNewUnit(@NotNull DataPoint dp, @NotNull Unit unit) {
        return new DataPoint(dp.getName(), dp.getValue(), unit, dp.getTime(), dp.getThreadId());
    }

}

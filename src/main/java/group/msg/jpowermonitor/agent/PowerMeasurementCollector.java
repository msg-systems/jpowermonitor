package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.MeasureMethodProvider;
import group.msg.jpowermonitor.agent.export.csv.CsvResultsWriter;
import group.msg.jpowermonitor.agent.export.prometheus.PrometheusWriter;
import group.msg.jpowermonitor.config.DefaultCfgProvider;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.config.dto.JavaAgentCfg;
import group.msg.jpowermonitor.dto.Activity;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.dto.MethodActivity;
import group.msg.jpowermonitor.dto.Quantity;
import group.msg.jpowermonitor.util.CpuAndThreadUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static group.msg.jpowermonitor.util.Constants.ONE_THOUSAND;

/**
 * Thread for collecting power statistics.
 */
@Slf4j
public class PowerMeasurementCollector extends TimerTask {
    /**
     * Power measurement method.
     */
    private static final MeasureMethod measureMethod;

    static {
        JPowerMonitorCfg config = new DefaultCfgProvider().readConfig(null);
        measureMethod = MeasureMethodProvider.resolveMeasureMethod(config);
    }

    private static final String CLASS_METHOD_SEPARATOR = ".";
    /**
     * Total energy consumption of application.
     */
    @Getter
    private final AtomicReference<DataPoint> energyConsumptionTotalInJoule =
        new AtomicReference<>(new DataPoint("energyConsumptionTotalInJoule", 0.0, Unit.JOULE, LocalDateTime.now(), null));
    private final Map<String, Long> threadsCpuTime = new HashMap<>();
    private final Map<String, DataPoint> energyConsumptionPerMethod = new ConcurrentHashMap<>();
    private final long measurementInterval;
    private final long gatherStatisticsInterval;
    private final double activityToEnergyRatio;
    /**
     * Process id.
     */
    @Getter
    private final long pid;
    private final ThreadMXBean threadMXBean;
    private static Set<String> packageFilter;
    private PrometheusWriter prometheusWriter;
    private final CsvResultsWriter csvResultsWriter;
    @Setter
    private long correctionMeasureStackActivityInMs;


    public PowerMeasurementCollector(long pid, ThreadMXBean threadMXBean, JavaAgentCfg javaAgentCfg) {
        this.measurementInterval = javaAgentCfg.getMeasurementIntervalInMs();
        this.gatherStatisticsInterval = javaAgentCfg.getGatherStatisticsIntervalInMs();
        // cast gatherStatisticsInterval to double in order to get double values.
        this.activityToEnergyRatio = measurementInterval > 0 ? (double) this.gatherStatisticsInterval / this.measurementInterval : 0.0;
        this.pid = pid;

        this.threadMXBean = threadMXBean;
        PowerMeasurementCollector.packageFilter = javaAgentCfg.getPackageFilter();
        if (javaAgentCfg.getMonitoring().getPrometheus().isEnabled()) {
            this.prometheusWriter = new PrometheusWriter(javaAgentCfg.getMonitoring().getPrometheus());
        }
        this.csvResultsWriter = new CsvResultsWriter();
    }

    @Override
    public void run() {
        Map<String, Set<MethodActivity>> methodActivityPerThread = new HashMap<>();
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        long duration = 0;
        while (duration < measurementInterval - correctionMeasureStackActivityInMs) { // 1 sec
            gatherMethodActivityPerThread(methodActivityPerThread, threads);
            duration += gatherStatisticsInterval; // 10 ms
            try { // Sleep for statisticsInterval, e.g. 10 ms
                TimeUnit.MILLISECONDS.sleep(gatherStatisticsInterval);
            } catch (InterruptedException ex) {
                log.error("sleep interrupted: {}", ex.getMessage());
            }
        }
        // Adds current power to total energy consumption of application
        DataPoint currentPower = getCurrentCpuPowerInWatts();

        // If the measurementInterval is 1000 ms = 1 sec, then it's fine to treat power and energy as equal here since power is measured each second and 1 joule = 1 watt / second.
        // If the interval is different from 1000 ms = 1 sec, then the power will be multiplied with the measurementInterval in order to get the energy over the interval.
        // It is assumed that the power is staying the same value for the whole interval.
        DataPoint currentEnergy = cloneAndCalculateDataPoint(currentPower, Unit.JOULE, val -> measurementInterval != 1000L ? val * measurementInterval : val);
        energyConsumptionTotalInJoule.getAndAccumulate(currentEnergy, this::addDataPoint);

        // CPU time for each thread
        long totalApplicationCpuTime = CpuAndThreadUtils.getTotalApplicationCpuTimeAndCalculateCpuTimePerApplicationThread(threadMXBean, threadsCpuTime, threads);
        Map<String, Double> powerPerThread = CpuAndThreadUtils.calculatePowerPerApplicationThread(threadsCpuTime, currentPower, totalApplicationCpuTime);

        // Now we have power for each thread, and stats for methods in each thread
        // We allocated power to each method based on activity
        allocateEnergyUsageToActivity(methodActivityPerThread, powerPerThread);

        List<Activity> activities = methodActivityPerThread
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Map<String, DataPoint> powerConsumption = aggregateActivityToDataPoints(activities, false);
        Map<String, DataPoint> filteredPowerConsumption = aggregateActivityToDataPoints(activities, true);
        csvResultsWriter.writePowerConsumptionPerMethod(powerConsumption);
        csvResultsWriter.writePowerConsumptionPerMethodFiltered(filteredPowerConsumption);
        if (prometheusWriter != null) {
            prometheusWriter.writePowerConsumptionPerMethodFiltered(filteredPowerConsumption);
        }
    }

    /**
     * Read power data from configured measure method
     *
     * @return current CPU power consumption in watts as reported by measure method
     */
    static DataPoint getCurrentCpuPowerInWatts() {
        return measureMethod.measureFirstConfiguredPath();
    }

    private static void gatherMethodActivityPerThread(Map<String, Set<MethodActivity>> methodActivityPerThread, Set<Thread> threads) {
        for (Thread thread : threads) {
            // Only consider threads that are currently running (not waiting or blocked)
            if (Thread.State.RUNNABLE == thread.getState()) {
                StackTraceElement[] stackTrace = thread.getStackTrace();
                if (stackTrace.length == 0) {
                    continue;
                }

                String threadName = thread.getName();
                methodActivityPerThread.putIfAbsent(threadName, new HashSet<>());
                MethodActivity activity = new MethodActivity();
                activity.setThreadName(threadName);
                activity.setTime(LocalDateTime.now());

                Arrays.stream(stackTrace)
                    .findFirst()
                    .map(PowerMeasurementCollector::getFullQualifiedMethodName)
                    .ifPresent(activity::setMethodQualifier);
                Arrays.stream(stackTrace)
                    .map(PowerMeasurementCollector::getFullQualifiedMethodName)
                    .filter(PowerMeasurementCollector::isMethodInFilterList)
                    .findFirst()
                    .ifPresent(activity::setFilteredMethodQualifier);

                methodActivityPerThread.get(threadName).add(activity);
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

    private void allocateEnergyUsageToActivity(Map<String, Set<MethodActivity>> methodActivityPerThread, Map<String, Double> powerPerApplicationThread) {
        for (Map.Entry<String, Set<MethodActivity>> entry : methodActivityPerThread.entrySet()) {
            String threadName = entry.getKey();

            for (MethodActivity activity : entry.getValue()) {
                Quantity methodPower = Quantity.of(powerPerApplicationThread.get(threadName) * activityToEnergyRatio, Unit.WATT);
                Quantity methodEnergy = Quantity.of(methodPower.getValue() * measurementInterval / ONE_THOUSAND, Unit.JOULE);
                if (methodEnergy.getValue() > 0) {
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

    public Map<String, DataPoint> getEnergyConsumptionPerMethod(boolean asFiltered) {
        return energyConsumptionPerMethod.entrySet().stream()
            .filter(e -> asFiltered ? isMethodInFilterList(e.getKey()) : e.getKey() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
            activity.getThreadName()
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
                activity -> activity.getIdentifier(filtered) + activity.getThreadName(),
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
            log.warn("not addable: dp1 = {}, dp2 = {}", dp1, dp2);
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
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        Arrays.stream(dataPoints).filter(dp -> areDataPointsAddable(reference, dp))
            .forEach(dp -> sum.getAndAccumulate(dp.getValue(), Double::sum));
        return new DataPoint(reference.getName(), sum.get(), reference.getUnit(), LocalDateTime.now(), reference.getThreadName());
    }

    /**
     * Create a new <code>DataPoint</code> but with new <code>unit</code>.
     * The <code>valueTransformer</code> can be used to calculate a new value in the new DataPoint.
     *
     * @param dp               <code>DataPoint</code> to clone
     * @param unit             new unit
     * @param valueTransformer a calculation instruction to transform the value in the new DataPoint.
     *                         For no transformation, specify identity <code>x -> x</code>.
     * @return <code>DataPoint</code> with new <code>unit</code>
     */
    public DataPoint cloneAndCalculateDataPoint(@NotNull DataPoint dp, @NotNull Unit unit, Function<Double, Double> valueTransformer) {
        return new DataPoint(dp.getName(), valueTransformer.apply(dp.getValue()), unit, dp.getTime(), dp.getThreadName());
    }
}

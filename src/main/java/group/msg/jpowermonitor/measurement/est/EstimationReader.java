package group.msg.jpowermonitor.measurement.est;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.dto.EstimationCfg;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.util.CpuAndThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the Estimation (see
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#energy-estimate-watt-hours">Energy Estimate (Watt-Hours)</a>)
 * measure method.
 *
 * @see MeasureMethod
 */
@Slf4j
public class EstimationReader implements MeasureMethod {
    private static final double EST_CPU_USAGE_FALLBACK = 0.5;
    public static final long MEASURE_TIME_ESTIMATION_MS = 100L;
    private static final String ESTIMATED_CPU_WATTS = "Estimated CPU Watts";
    private final EstimationCfg estCfg;

    public EstimationReader(JPowerMonitorCfg config) {
        Objects.requireNonNull(config.getMeasurement().getEst(), "Estimation config must be set!");
        this.estCfg = config.getMeasurement().getEst();
    }

    @Override
    public @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException {
        final double cpuUsage = getCpuUsage();
        double value = estCfg.getCpuMinWatts() + (cpuUsage * (estCfg.getCpuMaxWatts() - estCfg.getCpuMinWatts()));
        return new DataPoint(ESTIMATED_CPU_WATTS, value, Unit.WATT, LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Override
    public @NotNull List<String> configuredSensors() {
        return List.of(ESTIMATED_CPU_WATTS);
    }

    @Override
    public @NotNull Map<String, Double> defaultEnergyInIdleModeForMeasuredSensors() {
        return Map.of(ESTIMATED_CPU_WATTS, estCfg.getCpuMinWatts());
    }

    public double getCpuUsage() {
        // see https://www.cloudcarbonfootprint.org/docs/methodology/#energy-estimate-watt-hours
        long[] ids = CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication().getAllThreadIds();

        // Init measurement start time and CPU time
        long startTime = System.nanoTime();
        long startCpuTime = 0L;
        for (long id : ids) {
            startCpuTime += CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication().getThreadCpuTime(id);
        }

        // Wait for 100ms (WAIT_TIME_ESTIMATION_MS)
        try {
            TimeUnit.MILLISECONDS.sleep(MEASURE_TIME_ESTIMATION_MS);
        } catch (InterruptedException e) {
            log.info("Sleep was interrupted, ignoring");
        }

        // End measurement and add CPU time of all threads
        long endTime = System.nanoTime();
        long endCpuTime = 0L;
        for (long id : ids) {
            endCpuTime += CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication().getThreadCpuTime(id);
        }

        // Calculate approximated CPU usage in the last 100ms
        long elapsedCpu = endCpuTime - startCpuTime;
        long elapsedTime = endTime - startTime;
        double cpuUsage = (double) elapsedCpu / elapsedTime;

        if (cpuUsage <= 0) { // Fallback to 0.5 (50%) if CPU usage is negative or zero
            return EST_CPU_USAGE_FALLBACK;
        }
        // Fallback to 1 if CPU usage is greater than 1 - more than 100% is not possible ;)
        return Math.min(cpuUsage, 1);
    }

}

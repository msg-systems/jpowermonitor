package group.msg.jpowermonitor.measurement.est;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.EstimationCfg;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.util.CpuAndThreadUtils;

/**
 * Implementation of the Estimation (compare https://www.cloudcarbonfootprint.org/docs/methodology/#energy-estimate-watt-hours) measure method.
 *
 * @see MeasureMethod
 */
public class EstimationReader implements MeasureMethod {

    private static final String ESTIMATED_CPU_WATTS = "Estimated CPU Watts";

    private final JPowerMonitorConfig config;
    private final EstimationCfg estCfg;

    public EstimationReader(JPowerMonitorConfig config) {
        this.config = config;
        Objects.requireNonNull(config.getMeasurement().getEst(), "Estimation config must be set!");
        this.estCfg = config.getMeasurement().getEst();
    }

    @Override
    public @NotNull List<DataPoint> measure() throws JPowerMonitorException {
        return List.of(measureFirstConfiguredPath());
    }

    @Override
    public @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException {
        // Compare https://www.cloudcarbonfootprint.org/docs/methodology/#energy-estimate-watt-hours
        final double cpuUsage = CpuAndThreadUtils.getCpuUsage();
        BigDecimal value = BigDecimal.valueOf(estCfg.getCpuMinWatts() + (cpuUsage * (estCfg.getCpuMaxWatts() - estCfg.getCpuMinWatts())));
        //System.out.println("cpuUsage: " + cpuUsage + ", value: " + value + "W"); // DEBUG
        return new DataPoint(ESTIMATED_CPU_WATTS, value, Unit.WATT, LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Override
    public @NotNull List<String> configuredSensors() {
        return List.of(ESTIMATED_CPU_WATTS);
    }

    @Override
    public @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors() {
        return Map.of(ESTIMATED_CPU_WATTS, BigDecimal.valueOf(estCfg.getCpuMinWatts()));
    }

    @Override
    public int getSamplingInterval() {
        return config.getSamplingIntervalInMs();
    }

    @Override
    public int initCycles() {
        return config.getInitCycles();
    }

    @Override
    public int getSamplingIntervalForInit() {
        return config.getInitCycles();
    }

    @Override
    public int getCalmDownIntervalInMs() {
        return config.getCalmDownIntervalInMs();
    }

    @Override
    public @Nullable Path getPathToResultCsv() {
        return config.getCsvRecording().getResultCsv() != null ? Paths.get(
            config.getCsvRecording().getResultCsv()) : null;
    }

    @Override
    public @Nullable Path getPathToMeasurementCsv() {
        return config.getCsvRecording().getMeasurementCsv() != null ? Paths.get(
            config.getCsvRecording().getMeasurementCsv()) : null;
    }

    @Override
    public @NotNull BigDecimal getPercentageOfSamplesAtBeginningToDiscard() {
        return config.getPercentageOfSamplesAtBeginningToDiscard();
    }

}

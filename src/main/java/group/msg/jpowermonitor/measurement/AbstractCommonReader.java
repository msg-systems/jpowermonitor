package group.msg.jpowermonitor.measurement;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;

/**
 * Implementation of shared methods equal for all measure methods.
 *
 * @see MeasureMethod
 */
public abstract class AbstractCommonReader implements MeasureMethod {

    protected final JPowerMonitorConfig config;

    public AbstractCommonReader(JPowerMonitorConfig config) {
        this.config = config;
    }

    @Override
    public abstract @NotNull List<DataPoint> measure() throws JPowerMonitorException;

    @Override
    public abstract @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException;

    @Override
    public abstract @NotNull List<String> configuredSensors();

    @Override
    public abstract @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors();

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

package group.msg.jpowermonitor.util;

import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMeasureMethod implements MeasureMethod {
    private final JPowerMonitorConfig config;

    public FileMeasureMethod(JPowerMonitorConfig config) {
        this.config = config;
    }

    @Override
    public @NotNull List<DataPoint> measure() throws JPowerMonitorException {
        return Arrays.asList(measureFirst());
    }

    @Override
    public @NotNull DataPoint measureFirst() throws JPowerMonitorException {
        CsvMeasurementsReader reader = new CsvMeasurementsReader(config.getHwinfoCsvFile());
        Map<String, String> measurements = reader.readMeasurementsFromFile();
        return createDataPoint(measurements);
    }

    @NotNull
    private DataPoint createDataPoint(Map<String, String> measurements) {
        String value = measurements.get("\"CPU Package Power [W]\"");
        return new DataPoint(config.getHwinfoCsvFile(), new BigDecimal(value), Unit.WATT, LocalDateTime.now());
    }


    @Override
    public @NotNull List<String> configuredSensors() {
        return Arrays.asList(config.getHwinfoCsvFile()); // only ohwm
    }

    @Override
    public @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors() {
        return new HashMap<>(); // only ohwm
    }

    // same as ohwm TODO remove duplication
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
        return config.getSamplingIntervalForInitInMs();
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

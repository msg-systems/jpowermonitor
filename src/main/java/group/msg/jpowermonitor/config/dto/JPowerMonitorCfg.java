package group.msg.jpowermonitor.config.dto;

import group.msg.jpowermonitor.JPowerMonitorException;
import lombok.Data;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Data class for jPowerMonitor configuration.
 * Includes all configuration values.
 *
 * @see MeasurementCfg
 * @see CsvRecordingCfg
 * @see JavaAgentCfg
 */
@Data
public class JPowerMonitorCfg {
    private Integer samplingIntervalInMs;
    private Integer samplingIntervalForInitInMs;
    private Integer initCycles;
    private Integer calmDownIntervalInMs;
    private Double percentageOfSamplesAtBeginningToDiscard;
    private Double carbonDioxideEmissionFactor;
    private MeasurementCfg measurement;
    private CsvRecordingCfg csvRecording;
    private JavaAgentCfg javaAgent = new JavaAgentCfg();

    // special case of cached constants
    @Getter
    private static Double co2EmissionFactor;

    public void initializeConfiguration() {
        if (measurement == null || measurement.getMethod() == null) {
            throw new JPowerMonitorException("A measuring method must be defined!");
        }
        if ("lhm".equals(measurement.getMethod())) {
            if (measurement.getLhm() == null || measurement.getLhm().getUrl() == null) {
                throw new JPowerMonitorException("Libre Hardware Monitor REST endpoint URL must be configured");
            }
            measurement.getLhm().setUrl(measurement.getLhm().getUrl() + "/data.json");
            List<PathElementCfg> pathElems = measurement.getLhm().getPaths();
            if (pathElems == null
                || pathElems.isEmpty()
                || pathElems.get(0) == null
                || pathElems.get(0).getPath() == null
                || pathElems.get(0).getPath().isEmpty()) {
                throw new JPowerMonitorException("At least one path to a sensor value must be configured under paths");
            }
        } else {
            if (measurement.getCsv() == null || measurement.getCsv().getInputFile() == null || measurement.getCsv().getColumns() == null || measurement.getCsv().getColumns().isEmpty()) {
                throw new JPowerMonitorException("CSV input filepath and columns must be configured");
            }
        }
        setDefaultIfNotSet(samplingIntervalInMs, this::setSamplingIntervalInMs, 300);
        setDefaultIfNotSet(samplingIntervalForInitInMs, this::setSamplingIntervalForInitInMs, 1000);
        setDefaultIfNotSet(initCycles, this::setInitCycles, 10);
        setDefaultIfNotSet(calmDownIntervalInMs, this::setCalmDownIntervalInMs, 1000);
        setDefaultIfNotSet(percentageOfSamplesAtBeginningToDiscard, this::setPercentageOfSamplesAtBeginningToDiscard, 15.0);
        setDefaultIfNotSet(javaAgent.getMonitoring().getPrometheus().getHttpPort(), javaAgent.getMonitoring().getPrometheus()::setHttpPort, 1234);
        setDefaultIfNotSet(javaAgent.getMonitoring().getPrometheus().getWriteEnergyIntervalInS(), javaAgent.getMonitoring().getPrometheus()::setWriteEnergyIntervalInS, 30L);

        setCo2EmissionFactor(Objects.requireNonNullElse(carbonDioxideEmissionFactor, 485.0));
        setCarbonDioxideEmissionFactor(Objects.requireNonNullElse(carbonDioxideEmissionFactor, 485.0));
        javaAgent.setPackageFilter(Objects.requireNonNullElse(javaAgent.getPackageFilter(), Collections.emptySet()));
    }

    public static void setCo2EmissionFactor(Double carbonDioxideEmissionFactor) {
        JPowerMonitorCfg.co2EmissionFactor = carbonDioxideEmissionFactor;
    }

    private static <T> void setDefaultIfNotSet(T currentValue, Consumer<T> consumer, T defaultValue) {
        if (currentValue == null) {
            consumer.accept(defaultValue);
        }
    }
}

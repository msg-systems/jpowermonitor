package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Data
public class JPowerMonitorConfig {
    private Integer samplingIntervalInMs;
    private Integer samplingIntervalForInitInMs;
    private Integer initCycles;
    private Integer calmDownIntervalInMs;
    private BigDecimal percentageOfSamplesAtBeginningToDiscard;
    private BigDecimal kWhToCarbonDioxideEnergyMixFactor;
    private Measurement measurement;
    private CsvRecording csvRecording;
    private JavaAgent javaAgent;

    void initializeConfiguration() {
        if (measurement == null || measurement.getMethod() == null) {
            throw new JPowerMonitorException("A measuring method must be defined!");
        }
        if ("ohm".equals(measurement.getMethod())) {
            if (measurement.getOhm() == null || measurement.getOhm().getUrl() == null) {
                throw new JPowerMonitorException("OpenHardwareMonitor REST endpoint URL must be configured");
            }
            measurement.getOhm().setUrl(measurement.getOhm().getUrl() + "/data.json");
            List<PathElement> pathElems =  measurement.getOhm().getPaths();
            if (pathElems == null
                || pathElems.isEmpty()
                || pathElems.get(0) == null
                || pathElems.get(0).getPath() == null
                || pathElems.get(0).getPath().isEmpty()) {
                throw new JPowerMonitorException("At least one path to a sensor value must be configured under paths");
            }
        } else {
            if (measurement.getCsv() == null || measurement.getCsv().getInputFile() == null || measurement.getCsv().getColumns() == null || measurement.getCsv().getColumns().size() == 0) {
                throw new JPowerMonitorException("CSV input filepath and columns must be configured");
            }
        }
        setDefaultIfNotSet(samplingIntervalInMs, this::setSamplingIntervalInMs, 300);
        setDefaultIfNotSet(samplingIntervalForInitInMs, this::setSamplingIntervalForInitInMs, 1000);
        setDefaultIfNotSet(initCycles, this::setInitCycles, 10);
        setDefaultIfNotSet(calmDownIntervalInMs, this::setCalmDownIntervalInMs, 1000);
        setDefaultIfNotSet(percentageOfSamplesAtBeginningToDiscard, this::setPercentageOfSamplesAtBeginningToDiscard, new BigDecimal("15"));
        setDefaultIfNotSet(kWhToCarbonDioxideEnergyMixFactor, this::setKWhToCarbonDioxideEnergyMixFactor, new BigDecimal("485"));
        setDefaultIfNotSet(javaAgent, this::setJavaAgent, new JavaAgent());
        setDefaultIfNotSet(javaAgent.getPackageFilter(), javaAgent::setPackageFilter, Collections.emptySet());
    }

    private static <T> void setDefaultIfNotSet(T currentValue, Consumer<T> consumer, T defaultValue) {
        if (currentValue == null) {
            consumer.accept(defaultValue);
        }
    }
}

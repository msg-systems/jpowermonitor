package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.JPowerMonitorException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import lombok.Data;

@Data
public class JPowerMonitorConfig {

    private Integer samplingIntervalInMs;
    private Integer samplingIntervalForInitInMs;
    private Integer initCycles;
    private Integer calmDownIntervalInMs;
    private BigDecimal percentageOfSamplesAtBeginningToDiscard;
    private String measureMethod;
    private String hwinfoCsvFile;
    private OpenHardwareMonitor openHardwareMonitor;
    private CsvRecording csvRecording;
    private JavaAgent javaAgent;

    void initializeConfiguration() {
        if (openHardwareMonitor == null
            || openHardwareMonitor.getUrl() == null) {
            throw new JPowerMonitorException(
                "OpenHardwareMonitor REST endpoint URL must be configured");
        }

        openHardwareMonitor.setUrl(
            openHardwareMonitor.getUrl() + "/data.json");

        List<PathElement> pathElems = openHardwareMonitor.getPaths();
        if (pathElems == null
            || pathElems.isEmpty()
            || pathElems.get(0) == null
            || pathElems.get(0).getPath() == null
            || pathElems.get(0).getPath().isEmpty()) {
            throw new JPowerMonitorException(
                "At least one path to a sensor value must be configured under paths");
        }

        setDefaultIfNotSet(samplingIntervalInMs, this::setSamplingIntervalInMs, 300);
        setDefaultIfNotSet(samplingIntervalForInitInMs, this::setSamplingIntervalForInitInMs, 1000);
        setDefaultIfNotSet(initCycles, this::setInitCycles, 10);
        setDefaultIfNotSet(calmDownIntervalInMs, this::setCalmDownIntervalInMs, 1000);
        setDefaultIfNotSet(percentageOfSamplesAtBeginningToDiscard,
            this::setPercentageOfSamplesAtBeginningToDiscard, new BigDecimal("15"));
        setDefaultIfNotSet(javaAgent, this::setJavaAgent, new JavaAgent());
        setDefaultIfNotSet(javaAgent.getPackageFilter(), javaAgent::setPackageFilter,
            Collections.emptySet());
        setDefaultIfNotSet(measureMethod, this::setMeasureMethod,
            "ohwm");
    }

    private static <T> void setDefaultIfNotSet(T currentValue, Consumer<T> consumer,
        T defaultValue) {
        if (currentValue == null) {
            consumer.accept(defaultValue);
        }
    }


}

package group.msg.jpowermonitor.config;

import lombok.Data;

/**
 * Data class for measurement method.
 *
 * @see CsvMeasurementCfg
 * @see LibreHardwareMonitorCfg
 * @see EstimationCfg
 */
@Data
public class Measurement {
    private String method;
    private CsvMeasurementCfg csv;
    private LibreHardwareMonitorCfg lhm;
    private EstimationCfg est;
}

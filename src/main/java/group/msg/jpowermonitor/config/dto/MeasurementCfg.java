package group.msg.jpowermonitor.config.dto;

import lombok.Data;

/**
 * Data class for measurement method.
 *
 * @see CsvMeasurementCfg
 * @see LibreHardwareMonitorCfg
 * @see EstimationCfg
 */
@Data
public class MeasurementCfg {
    private String method; // sadly snakeyaml does not support using Enums as attributes.

    public MeasureMethodKey getMethodKey() {
        return MeasureMethodKey.of(method);
    }

    private CsvMeasurementCfg csv;
    private LibreHardwareMonitorCfg lhm;
    private EstimationCfg est;
}

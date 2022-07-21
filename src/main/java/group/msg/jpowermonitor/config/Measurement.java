package group.msg.jpowermonitor.config;

import lombok.Data;

@Data
public class Measurement {
    private String method;
    private CsvMeasurementCfg csv;
    private OpenHardwareMonitorCfg ohm;
}

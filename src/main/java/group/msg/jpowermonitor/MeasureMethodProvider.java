package group.msg.jpowermonitor;

import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.measurement.csv.CommaSeparatedValuesReader;
import group.msg.jpowermonitor.measurement.ohm.OpenHardwareMonitorReader;

public class MeasureMethodProvider {
    public static MeasureMethod resolveMeasureMethod(JPowerMonitorConfig config) {
        if ("csv".equals(config.getMeasurement().getMethod())) {
            return new CommaSeparatedValuesReader(config);
        } else if ("ohm".equals(config.getMeasurement().getMethod())) {
            return new OpenHardwareMonitorReader(config);
        } else {
            throw new JPowerMonitorException("Unknown measure method " + config.getMeasurement().getMethod());
        }
    }
}

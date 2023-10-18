package group.msg.jpowermonitor;

import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.measurement.csv.CommaSeparatedValuesReader;
import group.msg.jpowermonitor.measurement.lhm.LibreHardwareMonitorReader;

/**
 * Factory for creating the MeasureMethod from the config.
 *
 * @see MeasureMethod
 */
public class MeasureMethodProvider {
    public static MeasureMethod resolveMeasureMethod(JPowerMonitorConfig config) {
        if ("csv".equals(config.getMeasurement().getMethod())) {
            return new CommaSeparatedValuesReader(config);
        } else if ("lhm".equals(config.getMeasurement().getMethod())) {
            return new LibreHardwareMonitorReader(config);
        } else {
            throw new JPowerMonitorException("Unknown measure method " + config.getMeasurement().getMethod());
        }
    }
}

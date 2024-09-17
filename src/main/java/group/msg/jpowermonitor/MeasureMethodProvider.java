package group.msg.jpowermonitor;

import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.measurement.csv.CommaSeparatedValuesReader;
import group.msg.jpowermonitor.measurement.est.EstimationReader;
import group.msg.jpowermonitor.measurement.lhm.LibreHardwareMonitorReader;

/**
 * Factory for creating the MeasureMethod from the config.
 *
 * @see MeasureMethod
 */
public class MeasureMethodProvider {
    public static MeasureMethod resolveMeasureMethod(JPowerMonitorCfg config) {
        if ("csv".equals(config.getMeasurement().getMethod())) {
            return new CommaSeparatedValuesReader(config);
        } else if ("lhm".equals(config.getMeasurement().getMethod())) {
            return new LibreHardwareMonitorReader(config);
        } else if ("est".equals(config.getMeasurement().getMethod())) {
            return new EstimationReader(config);
        } else {
            throw new JPowerMonitorException("Unknown measure method " + config.getMeasurement().getMethod());
        }
    }
}

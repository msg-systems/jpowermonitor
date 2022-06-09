package group.msg.jpowermonitor;

import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.ohwm.MeasureOpenHwMonitor;
import group.msg.jpowermonitor.util.FileMeasureMethod;

public class MeasureMethodProvider {
    public static MeasureMethod resolveMeasureMethod(JPowerMonitorConfig config) {
        if ("hwinfo".equals(config.getMeasureMethod())) {
            return new FileMeasureMethod(config);
        } else if ("ohwm".equals(config.getMeasureMethod())) {
            return new MeasureOpenHwMonitor(config);
        } else {
            throw new JPowerMonitorException("Unknown measure method " + config.getMeasureMethod());
        }
    }
}

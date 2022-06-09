package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.MeasureMethodProvider;
import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.ohwm.MeasureOpenHwMonitor;

/**
 * Encapsulates concrete power measurement method and provides interface to get current cpu power consumption in watts.
 */
class MeasurePower {

    /**
     * Power measurement method
     */
    private static final MeasureMethod measureMethod;

    static {
        JPowerMonitorConfig config = new DefaultConfigProvider().readConfig(null);
        measureMethod = MeasureMethodProvider.resolveMeasureMethod(config);
    }

    /**
     * Read power data from configured measure method
     * @return current CPU power consumption in watts as reported by measure method
     */
    protected static DataPoint getCurrentCpuPowerInWatts() {
        return measureMethod.measureFirst();
    }

}

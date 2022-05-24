package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.MeasureMethod;
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
        // Init power measurement with OpenHardwareMonitor (TODO: Factory?)
        measureMethod = new MeasureOpenHwMonitor();
        measureMethod.init(null); // use default
    }

    /**
     * Read power data from configured measure method
     * @return current CPU power consumption in watts as reported by measure method
     */
    protected static DataPoint getCurrentCpuPowerInWatts() {
        return measureMethod.measureFirst();
    }

}

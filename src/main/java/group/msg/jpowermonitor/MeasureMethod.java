package group.msg.jpowermonitor;

import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Interface for different types of measuring the consumed energy.<br>
 * E.g. Libre Hardware Monitor or HWiNFO.
 */
public interface MeasureMethod {
    /**
     * Measure all data points for the configured paths.
     *
     * @return all data point for the configured paths.
     * @throws JPowerMonitorException if measurement tool is not available.
     */
    @NotNull List<DataPoint> measure() throws JPowerMonitorException;

    /**
     * Measure only the first configured path.
     *
     * @return the first data point in the configured paths.
     * @throws JPowerMonitorException if measurement tool is not available.
     */
    @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException;

    /**
     * @return list of configured sensor paths.
     */
    @NotNull List<String> configuredSensors();

    /**
     * The map of configured sensor paths with their configured default energy.
     * This is useful for a starting point to determine the energy in idle mode which may be
     * configured or measured in a separate method.
     *
     * @return Map of paths with default energy in idle mode (from config).
     */
    @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors();
}

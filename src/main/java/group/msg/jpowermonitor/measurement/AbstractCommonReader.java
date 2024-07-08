package group.msg.jpowermonitor.measurement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;

/**
 * Implementation of shared methods equal for all measure methods.
 *
 * @see MeasureMethod
 */
public abstract class AbstractCommonReader implements MeasureMethod {

    protected final JPowerMonitorConfig config;

    public AbstractCommonReader(JPowerMonitorConfig config) {
        this.config = config;
    }

    @Override
    public abstract @NotNull List<DataPoint> measure() throws JPowerMonitorException;

    @Override
    public abstract @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException;

    @Override
    public abstract @NotNull List<String> configuredSensors();

    @Override
    public abstract @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors();
}

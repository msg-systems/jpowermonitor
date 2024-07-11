package group.msg.jpowermonitor.measurement.est;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.MeasureMethod;
import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.EstimationCfg;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import group.msg.jpowermonitor.util.CpuAndThreadUtils;

/**
 * Implementation of the Estimation (see
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#energy-estimate-watt-hours">Energy Estimate (Watt-Hours)</a>)
 * measure method.
 *
 * @see MeasureMethod
 */
public class EstimationReader implements MeasureMethod {
    private static final String ESTIMATED_CPU_WATTS = "Estimated CPU Watts";
    private final EstimationCfg estCfg;

    public EstimationReader(JPowerMonitorConfig config) {
        Objects.requireNonNull(config.getMeasurement().getEst(), "Estimation config must be set!");
        this.estCfg = config.getMeasurement().getEst();
    }

    @Override
    public @NotNull DataPoint measureFirstConfiguredPath() throws JPowerMonitorException {
        final double cpuUsage = CpuAndThreadUtils.getCpuUsage();
        BigDecimal value = BigDecimal.valueOf(estCfg.getCpuMinWatts() + (cpuUsage * (estCfg.getCpuMaxWatts() - estCfg.getCpuMinWatts())));
        return new DataPoint(ESTIMATED_CPU_WATTS, value, Unit.WATT, LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Override
    public @NotNull List<String> configuredSensors() {
        return List.of(ESTIMATED_CPU_WATTS);
    }

    @Override
    public @NotNull Map<String, BigDecimal> defaultEnergyInIdleModeForMeasuredSensors() {
        return Map.of(ESTIMATED_CPU_WATTS, BigDecimal.valueOf(estCfg.getCpuMinWatts()));
    }
}

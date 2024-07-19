package group.msg.jpowermonitor.agent.export.statistics;

import group.msg.jpowermonitor.agent.PowerMeasurementCollector;
import group.msg.jpowermonitor.util.StressCpuExample;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

import static group.msg.jpowermonitor.util.Constants.SEPARATOR;
import static group.msg.jpowermonitor.util.Converter.convertJouleToKiloWattHours;
import static group.msg.jpowermonitor.util.Converter.convertJouleToWattHours;

public class StatisticsWriter {

    private final PowerMeasurementCollector powerMeasurementCollector;

    public StatisticsWriter(PowerMeasurementCollector powerMeasurementCollector) {
        this.powerMeasurementCollector = powerMeasurementCollector;
    }

    public void writeStatistics(Consumer<String> logger) {
        if (logger == null || powerMeasurementCollector == null
            || powerMeasurementCollector.getEnergyConsumptionTotalInJoule() == null
            || powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get() == null
            || powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue() == null) {
            return;
        }
        logger.accept(String.format("Application consumed %.2f joule - %.3f wh - %.6f kwh - %.3f gCO2 total",
            powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue(),
            convertJouleToWattHours(powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue()),
            convertJouleToKiloWattHours(powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue()),
            powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getCo2Value()));
        if (StressCpuExample.isBenchmarkRun()) {
            logger.accept(
                "Benchmark result efficiency factor (sum of all loop counters / energyConsumptionTotal): *** "
                + NumberFormat.getNumberInstance(Locale.GERMANY)
                    .format(StressCpuExample.getBenchmarkResult() / powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue().longValue())
                + " *** jPMarks");
        }
        //   logger.accept("Energy consumption per method and filtered methods written to '" + energyConsumptionPerMethodFileName + "' / '" + energyConsumptionPerFilteredMethodFileName + "'");
        logger.accept(SEPARATOR);
    }

}

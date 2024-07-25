package group.msg.jpowermonitor.agent.export.statistics;

import group.msg.jpowermonitor.agent.JPowerMonitorAgent;
import group.msg.jpowermonitor.agent.PowerMeasurementCollector;
import group.msg.jpowermonitor.agent.export.csv.CsvResultsWriter;
import group.msg.jpowermonitor.demo.StressCpuExample;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.util.Locale;

import static group.msg.jpowermonitor.util.Constants.SEPARATOR;
import static group.msg.jpowermonitor.util.Converter.convertJouleToKiloWattHours;
import static group.msg.jpowermonitor.util.Converter.convertJouleToWattHours;

@Slf4j
public class StatisticsWriter {
    private final PowerMeasurementCollector powerMeasurementCollector;

    public StatisticsWriter(PowerMeasurementCollector powerMeasurementCollector) {
        this.powerMeasurementCollector = powerMeasurementCollector;
    }

    public void writeStatistics(CsvResultsWriter csvResultsWriter) {
        if (powerMeasurementCollector == null
            || powerMeasurementCollector.getEnergyConsumptionTotalInJoule() == null
            || powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get() == null
            || powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue() == null) {
            return;
        }
        String appStatistics = String.format("Application consumed %.2f joule - %.3f wh - %.6f kwh - %.3f gCO2 total",
            powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue(),
            convertJouleToWattHours(powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue()),
            convertJouleToKiloWattHours(powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue()),
            powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getCo2Value());
        String benchmarkResult =
            "Benchmark result efficiency factor (sum of all loop counters / energyConsumptionTotal): *** "
            + NumberFormat.getNumberInstance(Locale.GERMANY)
                .format(StressCpuExample.getBenchmarkResult() / powerMeasurementCollector.getEnergyConsumptionTotalInJoule().get().getValue().longValue())
            + " *** jPMarks";
        String filesInfo = "Energy consumption per method written to '" + csvResultsWriter.getEnergyConsumptionPerMethodFileName()
                           + "' and filtered methods written to '" + csvResultsWriter.getEnergyConsumptionPerFilteredMethodFileName() + "'" + "\n" + SEPARATOR;

        if (JPowerMonitorAgent.isSlf4jLoggerImplPresent()) {
            log.info(appStatistics);
            log.info(benchmarkResult);
            log.info(filesInfo);
        } else {
            System.out.println(appStatistics);
            System.out.println(benchmarkResult);
            System.out.println(filesInfo);
        }
    }
}

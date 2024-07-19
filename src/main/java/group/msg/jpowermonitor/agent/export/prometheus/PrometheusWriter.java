package group.msg.jpowermonitor.agent.export.prometheus;

import group.msg.jpowermonitor.agent.export.ResultsWriter;
import group.msg.jpowermonitor.config.dto.PrometheusCfg;
import group.msg.jpowermonitor.dto.DataPoint;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static group.msg.jpowermonitor.util.Constants.APP_TITLE;
import static group.msg.jpowermonitor.util.Constants.LOG_PREFIX;

public class PrometheusWriter implements ResultsWriter {
    protected static final String METRICS_PREFIX = APP_TITLE + "_";
    private static final String energyConsumptionPerMethodMetricName = METRICS_PREFIX + "energy_per_method";
    private static final String powerConsumptionPerMethodMetricName = METRICS_PREFIX + "power_per_method";
    private static final String energyConsumptionPerFilteredMethodMetricName = METRICS_PREFIX + "energy_per_method_filtered";
    private static final String co2ConsumptionPerFilteredMethodMetricName = METRICS_PREFIX + "co2_per_method_filtered";
    private static final String powerConsumptionPerFilteredMethodMetricName = METRICS_PREFIX + "power_per_method_filtered";

    private static final String energyConsumptionPerFilteredMethodMetricHelp = "Energy for the filtered methods in Joules";
    private static final String powerConsumptionPerFilteredMethodMetricHelp = "Power for the filtered methods in Watts";
    private static final String co2ConsumptionPerFilteredMethodMetricHelp = "CO2 consumption of the filtered methods in grams";

    private static final CollectorRegistry registry = new CollectorRegistry();
    private static final Map<String, Gauge> gaugeMap = new HashMap<>();
    private final long pid;
    private static HTTPServer server;

    /**
     * Constructor
     *
     * @param prometheusCfg the prometheus config
     */
    public PrometheusWriter(PrometheusCfg prometheusCfg) {
        this.pid = ProcessHandle.current().pid();
        if (PrometheusWriter.server == null) {
            if (prometheusCfg.isPublishJvmMetrics()) {
                DefaultExports.initialize();
            }
            System.out.println(LOG_PREFIX + "Opening Http Server for jPowerMonitor Prometheus Metrics on port " + prometheusCfg.getHttpPort());
            try {
                PrometheusWriter.server = new HTTPServer(prometheusCfg.getHttpPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void writePowerConsumptionPerMethod(Map<String, DataPoint> measurements) {
        throw new IllegalArgumentException("Currently not implemented");
    }

    @Override
    public void writePowerConsumptionPerMethodFiltered(Map<String, DataPoint> measurements) {
        registerGaugeAndSetDataPoints(powerConsumptionPerFilteredMethodMetricName, measurements, pid, DataPoint::getValue);
    }

    @Override
    public void writeEnergyConsumptionPerMethod(Map<String, DataPoint> measurements) {
        throw new IllegalArgumentException("Currently not implemented");
    }

    @Override
    public void writeEnergyConsumptionPerMethodFiltered(Map<String, DataPoint> measurements) {
        registerGaugeAndSetDataPoints(energyConsumptionPerFilteredMethodMetricName, measurements, pid, DataPoint::getValue);
        registerGaugeAndSetDataPoints(co2ConsumptionPerFilteredMethodMetricName, measurements, pid, DataPoint::getCo2Value);
    }

    /**
     * @param metric        the name of the metric that is sent to prometheus
     * @param metrics       the DataPoints
     * @param pid           the process id.
     * @param valueSupplier a function to get the value for the time series to be published.
     */
    public void registerGaugeAndSetDataPoints(String metric, Map<String, DataPoint> metrics, long pid, Function<DataPoint, Double> valueSupplier) {
        Gauge gauge = gaugeMap.computeIfAbsent(metric,
            k -> Gauge.build()
                .name(metric)
                .labelNames("pid", "thread", "method")
                .help(helpForName(metric))
                .register());
        for (Map.Entry<String, DataPoint> entry : metrics.entrySet()) {
            DataPoint dp = entry.getValue();
            //                       pid,                   thread,             method            time series value
            gauge.labels(String.valueOf(pid), dp.getThreadName(), dp.getName()).set(valueSupplier.apply(dp));
        }
    }

    private String helpForName(String metric) {
        if (energyConsumptionPerFilteredMethodMetricName.equals(metric)) {
            return energyConsumptionPerFilteredMethodMetricHelp;
        } else if (powerConsumptionPerFilteredMethodMetricName.equals(metric)) {
            return powerConsumptionPerFilteredMethodMetricHelp;
        } else if (co2ConsumptionPerFilteredMethodMetricName.equals(metric)) {
            return co2ConsumptionPerFilteredMethodMetricHelp;
        } else {
            throw new IllegalArgumentException("Unknown metric. Configure help for " + metric);
        }
    }

    public void deregisterGauges() {
        for (Gauge gauge : gaugeMap.values()) {
            registry.unregister(gauge);
        }
        gaugeMap.clear();
    }
}

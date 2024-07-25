package group.msg.jpowermonitor.agent.export.prometheus;

import group.msg.jpowermonitor.agent.export.ResultsWriter;
import group.msg.jpowermonitor.config.dto.PrometheusCfg;
import group.msg.jpowermonitor.dto.DataPoint;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static group.msg.jpowermonitor.util.Constants.APP_TITLE;

/**
 * Prometheus Writer to write energy, co2 and power per filtered method to prometheus.
 */
@Slf4j
public class PrometheusWriter implements ResultsWriter {
    protected static final String METRICS_PREFIX = APP_TITLE + "_";
    private static final String ENERGY_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME = METRICS_PREFIX + "energy_per_method_filtered";
    private static final String CO2_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME = METRICS_PREFIX + "co2_per_method_filtered";
    private static final String POWER_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME = METRICS_PREFIX + "power_per_method_filtered";

    private static final String ENERGY_CONSUMPTION_PER_FILTERED_METHOD_METRIC_HELP = "Energy for the filtered methods in Joules";
    private static final String POWER_CONSUMPTION_PER_FILTERED_METHOD_METRIC_HELP = "Power for the filtered methods in Watts";
    private static final String CO2_CONSUMPTION_PER_FILTERED_METHOD_METRIC_HELP = "CO2 consumption of the filtered methods in grams";

    private static final CollectorRegistry registry = new CollectorRegistry();
    private static final Map<String, Gauge> gaugeMap = new ConcurrentHashMap<>();
    private final long pid;
    private static HTTPServer server;
    private static final Lock lock = new ReentrantLock();

    /**
     * Constructor
     *
     * @param prometheusCfg the prometheus config
     */
    public PrometheusWriter(PrometheusCfg prometheusCfg) {
        this.pid = ProcessHandle.current().pid();
        if (lock.tryLock()) {
            try {
                if (PrometheusWriter.server == null) {
                    if (prometheusCfg.isPublishJvmMetrics()) {
                        DefaultExports.initialize();
                    }
                    log.info("Opening Http Server for jPowerMonitor Prometheus Metrics on port " + prometheusCfg.getHttpPort());
                    try {
                        PrometheusWriter.server = new HTTPServer(prometheusCfg.getHttpPort());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void writePowerConsumptionPerMethod(Map<String, DataPoint> measurements) {
        throw new IllegalArgumentException("Currently not implemented");
    }

    @Override
    public void writePowerConsumptionPerMethodFiltered(Map<String, DataPoint> measurements) {
        registerGaugeAndSetDataPoints(POWER_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME, measurements, pid, DataPoint::getValue);
    }

    @Override
    public void writeEnergyConsumptionPerMethod(Map<String, DataPoint> measurements) {
        throw new IllegalArgumentException("Currently not implemented");
    }

    @Override
    public void writeEnergyConsumptionPerMethodFiltered(Map<String, DataPoint> measurements) {
        registerGaugeAndSetDataPoints(ENERGY_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME, measurements, pid, DataPoint::getValue);
        registerGaugeAndSetDataPoints(CO2_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME, measurements, pid, DataPoint::getCo2Value);
    }

    /**
     * @param metric        the name of the metric that is sent to prometheus
     * @param metrics       the DataPoints
     * @param pid           the process id.
     * @param valueSupplier a function to get the value for the time series to be published.
     */
    public void registerGaugeAndSetDataPoints(String metric, Map<String, DataPoint> metrics, long pid, Function<DataPoint, Double> valueSupplier) {
        log.debug("writing " + metric + ", metrics.size:" + metrics.size());
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
        if (ENERGY_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME.equals(metric)) {
            return ENERGY_CONSUMPTION_PER_FILTERED_METHOD_METRIC_HELP;
        } else if (POWER_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME.equals(metric)) {
            return POWER_CONSUMPTION_PER_FILTERED_METHOD_METRIC_HELP;
        } else if (CO2_CONSUMPTION_PER_FILTERED_METHOD_METRIC_NAME.equals(metric)) {
            return CO2_CONSUMPTION_PER_FILTERED_METHOD_METRIC_HELP;
        } else {
            throw new IllegalArgumentException("Unknown metric. Configure help for " + metric);
        }
    }
}

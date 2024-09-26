package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.agent.export.csv.CsvResultsWriter;
import group.msg.jpowermonitor.agent.export.prometheus.PrometheusWriter;
import group.msg.jpowermonitor.agent.export.statistics.StatisticsWriter;
import group.msg.jpowermonitor.config.DefaultCfgProvider;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.config.dto.JavaAgentCfg;
import group.msg.jpowermonitor.config.dto.MeasureMethodKey;
import group.msg.jpowermonitor.measurement.est.EstimationReader;
import group.msg.jpowermonitor.util.Constants;
import group.msg.jpowermonitor.util.CpuAndThreadUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ThreadMXBean;
import java.util.Timer;
import java.util.TimerTask;

import static group.msg.jpowermonitor.util.Constants.SEPARATOR;

/**
 * Implements java agent to introspect power consumption of any java application.
 * <br><br>
 * Usage:<br>
 * <code>java -javaagent:jpowermonitor-1.0.3-SNAPSHOT-all.jar[=path-to-jpowermonitor.yaml] -jar MyApp.jar [args]</code>
 *
 * @author deinerj
 */
@Slf4j
public class JPowerMonitorAgent {
    private static final int ONE_SECOND_IN_MILLIS = 1000;
    @Getter
    private static final boolean slf4jLoggerImplPresent = !LoggerFactory.getILoggerFactory().getLogger("JPowerMonitorAgent").getName().equals("NOP");

    private JPowerMonitorAgent() {
    }

    /**
     * Hook to initialize the power measurement java agent at JVM startup.<br>
     * Afterward the original app <code>main</code>-Method will be called.
     *
     * @param args command line args
     * @param inst java agent params
     */
    public static void premain(String args, Instrumentation inst) {
        long pid = ProcessHandle.current().pid();
        JPowerMonitorCfg cfg = new DefaultCfgProvider().readConfig(args);
        MeasureMethodKey measureMethodKey = cfg.getMeasurement().getMethodKey();
        String appInfo = String.format("Measuring power with %s, Version %s (Pid: %s) using measure method '%s'",
            Constants.APP_TITLE,
            JPowerMonitorAgent.class.getPackage().getImplementationVersion(),
            pid,
            measureMethodKey.getName());

        if (isSlf4jLoggerImplPresent()) {
            log.info(appInfo);
        } else {
            System.out.println(appInfo);
        }
        log.info(SEPARATOR);
        ThreadMXBean threadMXBean = CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication();

        JavaAgentCfg javaAgentCfg = cfg.getJavaAgent();
        log.debug("Start monitoring application with PID {}, javaAgentCfg.getMeasurementIntervalInMs(): {}", pid, javaAgentCfg.getMeasurementIntervalInMs());
        // TimerTask to calculate power consumption per thread at runtime using a configurable measurement interval
        // start Timer as daemon thread, so that it does not prevent applications from stopping
        Timer powerMeasurementTimer = new Timer("PowerMeasurementCollector", true);
        Timer energyToCsvTimer = new Timer("CsvResultsWriter", true);
        Timer energyToPrometheusTimer = new Timer("PrometheusWriter", true);

        PowerMeasurementCollector powerMeasurementCollector = new PowerMeasurementCollector(pid, threadMXBean, javaAgentCfg);
        if (MeasureMethodKey.EST.equals(measureMethodKey)) {
            // as the estimation method is sleeping for a certain time while measuring the power, correct the wait time
            // in the power measure collector by that period of time.
            powerMeasurementCollector.setCorrectionMeasureStackActivityInMs(EstimationReader.MEASURE_TIME_ESTIMATION_MS);

            // for the other methods there is also a small delay depending on the hardware running on. This may vary between 10 and 40 ms.
            // Ignore this for the moment...
        }
        long delayAndPeriodPmc = javaAgentCfg.getMeasurementIntervalInMs();
        powerMeasurementTimer.schedule(powerMeasurementCollector, delayAndPeriodPmc, delayAndPeriodPmc);
        log.debug("Scheduled PowerMeasurementCollector with delay {} ms and period {} ms", delayAndPeriodPmc, delayAndPeriodPmc);
        // TimerTask to write energy measurement statistics to CSV files while application still running
        if (javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() > 0) {
            CsvResultsWriter cw = new CsvResultsWriter();
            long delayAndPeriodCw = javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() * ONE_SECOND_IN_MILLIS;
            // start Timer as daemon thread, so that it does not prevent applications from stopping
            energyToCsvTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        cw.writeEnergyConsumptionPerMethod(powerMeasurementCollector.getEnergyConsumptionPerMethod(false));
                        cw.writeEnergyConsumptionPerMethodFiltered(powerMeasurementCollector.getEnergyConsumptionPerMethod(true));
                    }
                }, delayAndPeriodCw, delayAndPeriodCw);
            log.debug("Scheduled CsvResultsWriter with delay {} ms and period {} ms", delayAndPeriodCw, delayAndPeriodCw);
        }
        if (javaAgentCfg.getMonitoring().getPrometheus().isEnabled()) {
            PrometheusWriter pw = new PrometheusWriter(javaAgentCfg.getMonitoring().getPrometheus());
            long delayAndPeriodPw = javaAgentCfg.getMonitoring().getPrometheus().getWriteEnergyIntervalInS() * ONE_SECOND_IN_MILLIS;
            energyToPrometheusTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        pw.writeEnergyConsumptionPerMethodFiltered(powerMeasurementCollector.getEnergyConsumptionPerMethod(true));
                    }
                }, delayAndPeriodPw, delayAndPeriodPw);
            log.debug("Scheduled PrometheusWriter with delay {} ms and period {} ms", delayAndPeriodPw, delayAndPeriodPw);
        }

        // Gracefully stop measurement at application shutdown
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                powerMeasurementTimer.cancel();
                powerMeasurementTimer.purge();
                energyToCsvTimer.cancel();
                energyToCsvTimer.purge();
                energyToPrometheusTimer.cancel();
                energyToPrometheusTimer.purge();
                log.info("Power measurement ended gracefully");
            })
        );
        // at shutdown write last results to CSV files and write statistics
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CsvResultsWriter rw = new CsvResultsWriter();
            rw.writeEnergyConsumptionPerMethod(powerMeasurementCollector.getEnergyConsumptionPerMethod(false));
            rw.writeEnergyConsumptionPerMethodFiltered(powerMeasurementCollector.getEnergyConsumptionPerMethod(true));
            new StatisticsWriter(powerMeasurementCollector).writeStatistics(rw);
        }));
    }
}

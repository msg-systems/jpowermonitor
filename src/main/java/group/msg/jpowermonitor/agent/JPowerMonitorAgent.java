package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.agent.export.csv.CsvResultsWriter;
import group.msg.jpowermonitor.agent.export.prometheus.PrometheusWriter;
import group.msg.jpowermonitor.agent.export.statistics.StatisticsWriter;
import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.config.dto.JavaAgentCfg;
import group.msg.jpowermonitor.util.Constants;
import group.msg.jpowermonitor.util.CpuAndThreadUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;
import java.lang.management.ThreadMXBean;
import java.util.Timer;
import java.util.TimerTask;

import static group.msg.jpowermonitor.util.Constants.LOG_PREFIX;
import static group.msg.jpowermonitor.util.Constants.SEPARATOR;


/**
 * Implements java agent to introspect power consumption of any java application.
 * <br><br>
 * Usage:<br>
 * <code>java -javaagent:jpowermonitor-1.0.2-SNAPSHOT-all.jar[=path-to-jpowermonitor.yaml] -jar MyApp.jar [args]</code>
 *
 * @author deinerj
 */
public class JPowerMonitorAgent {
    private static final int ONE_SECOND_IN_MILLIES = 1000;
    private static Timer timer;
    private static PowerMeasurementCollector powerMeasurementCollector;
    private static Timer writeEnergyMeasurementResultsToCsv;
    private static Timer writeEnergyMeasurementResultsToPrometheus;

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
        System.out.println("Measuring power with " + Constants.APP_TITLE + ", Version " + JPowerMonitorAgent.class.getPackage().getImplementationVersion());
        System.out.println(SEPARATOR);
        ThreadMXBean threadMXBean = CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication();
        long pid = ProcessHandle.current().pid();
        JPowerMonitorCfg cfg = new DefaultConfigProvider().readConfig(args);
        JavaAgentCfg javaAgentCfg = cfg.getJavaAgent();
        System.out.println(LOG_PREFIX + Thread.currentThread().getName() + ": Start monitoring application with PID " + pid);

        // TimerTask to calculate power consumption per thread at runtime using a configurable measurement interval
        // start Timer as daemon thread, so that it does not prevent applications from stopping
        timer = new Timer("PowerStatistics-Thread", true);
        powerMeasurementCollector = new PowerMeasurementCollector(pid, threadMXBean, javaAgentCfg);
        timer.schedule(powerMeasurementCollector, javaAgentCfg.getGatherStatisticsIntervalInMs(), javaAgentCfg.getGatherStatisticsIntervalInMs());

        // TimerTask to write energy measurement statistics to CSV files while application still running
        if (javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() > 0) {
            CsvResultsWriter rw = new CsvResultsWriter();
            // start Timer as daemon thread, so that it does not prevent applications from stopping
            writeEnergyMeasurementResultsToCsv = new Timer("ResultsWriter", true);
            writeEnergyMeasurementResultsToCsv.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        rw.writeEnergyConsumptionPerMethod(powerMeasurementCollector.getEnergyConsumptionPerMethod(false));
                        rw.writeEnergyConsumptionPerMethodFiltered(powerMeasurementCollector.getEnergyConsumptionPerMethod(true));
                    }
                }, javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() * ONE_SECOND_IN_MILLIES,
                javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() * ONE_SECOND_IN_MILLIES);
        }
        if (javaAgentCfg.getMonitoring().getPrometheus().isEnabled()) {
            writeEnergyMeasurementResultsToPrometheus = new Timer("PrometheusWriter", true);
            PrometheusWriter pw = new PrometheusWriter(javaAgentCfg.getMonitoring().getPrometheus());
            writeEnergyMeasurementResultsToPrometheus.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        // pw.writeEnergyConsumptionPerMethod(powerMeasurementCollector.getEnergyConsumptionPerMethod(false));
                        pw.writeEnergyConsumptionPerMethodFiltered(powerMeasurementCollector.getEnergyConsumptionPerMethod(true));
                    }
                }, javaAgentCfg.getMonitoring().getPrometheus().getWriteEnergyIntervalInS() * ONE_SECOND_IN_MILLIES,
                javaAgentCfg.getMonitoring().getPrometheus().getWriteEnergyIntervalInS() * ONE_SECOND_IN_MILLIES);
        }
        // Gracefully stop measurement at application shutdown
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                powerMeasurementCollector.cancel();
                timer.cancel();
                timer.purge();
                if (writeEnergyMeasurementResultsToCsv != null) {
                    writeEnergyMeasurementResultsToCsv.cancel();
                    writeEnergyMeasurementResultsToCsv.purge();
                }
                if (writeEnergyMeasurementResultsToPrometheus != null) {
                    writeEnergyMeasurementResultsToPrometheus.cancel();
                    writeEnergyMeasurementResultsToPrometheus.purge();
                }
                System.out.println("Power measurement ended gracefully");
            })
        );

        // At shutdown write last results to CSV files
        Runtime.getRuntime().addShutdownHook(new Thread(createEnergyConsumptionWriter(cfg)));
        // at shutdown write statistics
        Thread statisticsThread = new Thread(() -> new StatisticsWriter(powerMeasurementCollector).writeStatistics(System.out::println));
        Runtime.getRuntime().addShutdownHook(statisticsThread);
    }

    private static @NotNull Runnable createEnergyConsumptionWriter(JPowerMonitorCfg cfg) {
        // At shutdown write missing power metrics since last scheduled call.
        return () -> {
            CsvResultsWriter rw = new CsvResultsWriter();
            rw.writeEnergyConsumptionPerMethod(powerMeasurementCollector.getEnergyConsumptionPerMethod(false));
            rw.writeEnergyConsumptionPerMethodFiltered(powerMeasurementCollector.getEnergyConsumptionPerMethod(true));
        };
    }
}

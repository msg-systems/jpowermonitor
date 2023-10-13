package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.config.JavaAgent;
import group.msg.jpowermonitor.util.Constants;

import java.lang.instrument.Instrumentation;
import java.lang.management.ThreadMXBean;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static group.msg.jpowermonitor.agent.ResultsWriter.SEPARATOR;


/**
 * Implements java agent to introspect power consumption of any java application.
 * <br><br>
 * Usage:<br>
 * <code>java -javaagent:jpowermonitor-0.1.0-SNAPSHOT-all.jar[=path-to-jpowermonitor.yaml] -jar MyApp.jar [args]</code>
 *
 * @author deinerj
 */
public class JPowerMonitorAgent {
    private static final int ONE_SECOND_IN_MILLIES = 1000;
    private static Timer timer;
    private static PowerStatistics powerStatistics;
    private static Timer writeEnergyMeasurementResultsToCsv;

    private JPowerMonitorAgent() {
    }

    /**
     * Hook to initialize the power measurement java agent at JVM startup.<br>
     * Afterwards the original app <code>main</code>-Method will be called.
     *
     * @param args command line args
     * @param inst java agent params
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("Measuring power with " + Constants.APP_TITLE + ", Version " + JPowerMonitorAgent.class.getPackage().getImplementationVersion());
        System.out.println(SEPARATOR);
        ThreadMXBean threadMXBean = CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication();
        long pid = ProcessHandle.current().pid();
        JPowerMonitorConfig cfg = new DefaultConfigProvider().readConfig(args);
        JavaAgent javaAgentCfg = cfg.getJavaAgent();
        Set<String> packageFilter = javaAgentCfg.getPackageFilter();
        System.out.println(Thread.currentThread().getName() + ": Start monitoring application with PID " + pid);

        // TimerTask to calculate power consumption per thread at runtime using a configurable measurement interval
        timer = new Timer("PowerStatistics-Thread", true); // start Timer as daemon thread, so that it does not prevent applications from stopping
        powerStatistics = new PowerStatistics(javaAgentCfg.getMeasurementIntervalInMs(), javaAgentCfg.getGatherStatisticsIntervalInMs(), pid, threadMXBean, packageFilter);
        timer.schedule(powerStatistics, javaAgentCfg.getGatherStatisticsIntervalInMs(), javaAgentCfg.getGatherStatisticsIntervalInMs());

        // TimerTask to write energy measurement statistics to CSV files while application still running
        if (javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() > 0) {
            writeEnergyMeasurementResultsToCsv = new Timer("ResultsWriter-Thread", true); // start Timer as daemon thread, so that it does not prevent applications from stopping
            writeEnergyMeasurementResultsToCsv.schedule(new TimerTask() {
                @Override
                public void run() {
                    ResultsWriter rw = new ResultsWriter(powerStatistics, false, cfg.getCarbonDioxideEmissionFactor());
                    rw.execute();
                }
            }, javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() * ONE_SECOND_IN_MILLIES, javaAgentCfg.getWriteEnergyMeasurementsToCsvIntervalInS() * ONE_SECOND_IN_MILLIES);
        }

        // Gracefully stop measurement at application shutdown
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                powerStatistics.cancel();
                timer.cancel();
                timer.purge();
                if (writeEnergyMeasurementResultsToCsv != null) {
                    writeEnergyMeasurementResultsToCsv.cancel();
                    writeEnergyMeasurementResultsToCsv.purge();
                }
                System.out.println("Power measurement ended gracefully");
            }
            ));

        // Write results to CSV files
        Thread powerStatThread = new Thread(new ResultsWriter(powerStatistics, true, cfg.getCarbonDioxideEmissionFactor()));
        powerStatThread.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(powerStatThread);
    }
}

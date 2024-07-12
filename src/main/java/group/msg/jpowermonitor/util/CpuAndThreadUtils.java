package group.msg.jpowermonitor.util;

import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static group.msg.jpowermonitor.util.Constants.ONE_HUNDRED;

/**
 * Utility class for all CPU and thread time/power related tasks
 *
 * @author deinerj
 */
public class CpuAndThreadUtils {
    private static final double EST_CPU_USAGE_FALLBACK = 0.5;
    private static ThreadMXBean threadMXBean;

    @NotNull
    public static ThreadMXBean initializeAndGetThreadMxBeanOrFailAndQuitApplication() {
        if (threadMXBean == null) {
            threadMXBean = ManagementFactory.getThreadMXBean();
            // Check if CPU Time measurement is supported by the JVM. Quit otherwise
            if (!threadMXBean.isThreadCpuTimeSupported()) {
                System.err.println("Thread CPU Time is not supported in this JVM, unable to measure energy consumption.");
                System.exit(1);
            }
            // Enable CPU Time measurement if it is disabled
            if (!threadMXBean.isThreadCpuTimeEnabled()) {
                threadMXBean.setThreadCpuTimeEnabled(true);
            }
        }
        return threadMXBean;
    }

    public static long getTotalApplicationCpuTimeAndCalculateCpuTimePerApplicationThread(ThreadMXBean threadMxBean, Map<String, Long> cpuTimePerApplicationThread, Set<Thread> applicationThreads) {
        long totalApplicationCpuTime = 0L;
        for (Thread t : applicationThreads) {
            long applicationThreadCpuTime = threadMxBean.getThreadCpuTime(t.getId());

            // If thread already monitored, then calculate CPU time since last time
            if (cpuTimePerApplicationThread.containsKey(t.getName())) {
                applicationThreadCpuTime -= cpuTimePerApplicationThread.get(t.getName());
            }

            cpuTimePerApplicationThread.put(t.getName(), applicationThreadCpuTime);
            totalApplicationCpuTime += applicationThreadCpuTime;
        }
        return totalApplicationCpuTime;
    }

    @NotNull
    public static Map<String, Double> calculatePowerPerApplicationThread(Map<String, Long> cpuTimePerApplicationThread, DataPoint currentPower, long totalApplicationCpuTime) {
        Map<String, Double> powerPerApplicationThread = new HashMap<>();
        for (Map.Entry<String, Long> entry : cpuTimePerApplicationThread.entrySet()) {
            Double percentageCpuTimePerApplicationThread = totalApplicationCpuTime > 0 ? entry.getValue() * ONE_HUNDRED / totalApplicationCpuTime : 0.0;
            Double applicationThreadPower = currentPower.getValue() * percentageCpuTimePerApplicationThread / ONE_HUNDRED;
            powerPerApplicationThread.put(entry.getKey(), applicationThreadPower);
        }
        return powerPerApplicationThread;
    }

    public static double getCpuUsage() {
        // see https://www.cloudcarbonfootprint.org/docs/methodology/#energy-estimate-watt-hours
        long[] ids = CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication().getAllThreadIds();

        // Init measurement start time and CPU time
        long startTime = System.nanoTime();
        long startCpuTime = 0L;
        for (long id : ids) {
            startCpuTime += CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication().getThreadCpuTime(id);
        }

        // Wait for 100ms
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            System.err.println("Sleep was interrupted, ignoring");
        }

        // End measurement and add CPU time of all threads
        long endTime = System.nanoTime();
        long endCpuTime = 0L;
        for (long id : ids) {
            endCpuTime += CpuAndThreadUtils.initializeAndGetThreadMxBeanOrFailAndQuitApplication().getThreadCpuTime(id);
        }

        // Calculate approximated CPU usage in the last 100ms
        long elapsedCpu = endCpuTime - startCpuTime;
        long elapsedTime = endTime - startTime;
        double cpuUsage = (double) elapsedCpu / elapsedTime;

        if (cpuUsage <= 0) { // Fallback to 0.5 (50%) if CPU usage is negative or zero
            return EST_CPU_USAGE_FALLBACK;
        }
        // Fallback to 1 if CPU usage is greater than 1 - more than 100% is not possible ;)
        return Math.min(cpuUsage, 1);
    }
}

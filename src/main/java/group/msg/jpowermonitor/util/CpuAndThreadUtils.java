package group.msg.jpowermonitor.util;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CpuAndThreadUtils {

    private static ThreadMXBean threadMXBean;

    @NotNull
    public static ThreadMXBean initializeAndGetThreadMxBeanOrFailAndQuitApplication() {
        if (threadMXBean == null) {
            threadMXBean = ManagementFactory.getThreadMXBean();
            // Check if CPU Time measurement is supported by the JVM. Quit otherwise
            if (!threadMXBean.isThreadCpuTimeSupported()) {
                log.error("Thread CPU Time is not supported in this JVM, unable to measure energy consumption.");
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
            long applicationThreadCpuTime = threadMxBean.getThreadCpuTime(t.getId()); // use t.threadId() with JDK 21

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


}

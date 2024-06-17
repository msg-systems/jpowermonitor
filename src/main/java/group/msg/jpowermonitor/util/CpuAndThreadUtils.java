package group.msg.jpowermonitor.util;

import group.msg.jpowermonitor.dto.DataPoint;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static group.msg.jpowermonitor.util.Constants.MATH_CONTEXT;
import static group.msg.jpowermonitor.util.Constants.ONE_HUNDRED;

/**
 * Utility class for all CPU and thread time/power related tasks
 *
 * @author deinerj
 */
public class CpuAndThreadUtils {

    private static ThreadMXBean threadMXBean;

    @NotNull
    public static ThreadMXBean initializeAndGetThreadMxBeanOrFailAndQuitApplication() {
        if (threadMXBean == null)  {
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
        long totalApplicationCpuTime = 0;
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
    public static Map<String, BigDecimal> calculatePowerPerApplicationThread(Map<String, Long> cpuTimePerApplicationThread, DataPoint currentPower, long totalApplicationCpuTime) {
        Map<String, BigDecimal> powerPerApplicationThread = new HashMap<>();
        for (Map.Entry<String, Long> entry : cpuTimePerApplicationThread.entrySet()) {
            BigDecimal percentageCpuTimePerApplicationThread =
                totalApplicationCpuTime > 0 ? new BigDecimal(entry.getValue()).multiply(ONE_HUNDRED, MATH_CONTEXT).divide(new BigDecimal(totalApplicationCpuTime), MATH_CONTEXT) : BigDecimal.ZERO;
            BigDecimal applicationThreadPower = currentPower.getValue().multiply(percentageCpuTimePerApplicationThread.divide(ONE_HUNDRED, MATH_CONTEXT), MATH_CONTEXT);
            powerPerApplicationThread.put(entry.getKey(), applicationThreadPower);
        }
        return powerPerApplicationThread;
    }
}

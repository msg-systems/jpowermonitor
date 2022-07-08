package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for all CPU and thread time/power related tasks
 *
 * @author deinerj
 */
public class CpuAndThreadUtils {
    private static final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);

    @NotNull
    static ThreadMXBean initializeAndGetThreadMxBeanOrFailAndQuitApplication() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        // Check if CPU Time measurement is supported by the JVM. Quit otherwise
        if (!threadMxBean.isThreadCpuTimeSupported()) {
            System.err.println("Thread CPU Time is not supported in this JVM, unable to measure energy consumption.");
            System.exit(1);
        }

        // Enable CPU Time measurement if it is disabled
        if (!threadMxBean.isThreadCpuTimeEnabled()) {
            threadMxBean.setThreadCpuTimeEnabled(true);
        }
        return threadMxBean;
    }

    static long getTotalApplicationCpuTimeAndCalculateCpuTimePerApplicationThread(ThreadMXBean threadMxBean, Map<Long, Long> cpuTimePerApplicationThread, Set<Thread> applicationThreads) {
        long totalApplicationCpuTime = 0;
        for (Thread t : applicationThreads) {
            long applicationThreadCpuTime = threadMxBean.getThreadCpuTime(t.getId());

            // If thread already monitored, then calculate CPU time since last time
            if (cpuTimePerApplicationThread.containsKey(t.getId())) {
                applicationThreadCpuTime -= cpuTimePerApplicationThread.get(t.getId());
            }

            cpuTimePerApplicationThread.put(t.getId(), applicationThreadCpuTime);
            totalApplicationCpuTime += applicationThreadCpuTime;
        }
        return totalApplicationCpuTime;
    }

    @NotNull
    static Map<Long, BigDecimal> calculatePowerPerApplicationThread(Map<Long, Long> cpuTimePerApplicationThread, DataPoint currentPower, long totalApplicationCpuTime) {
        Map<Long, BigDecimal> powerPerApplicationThread = new HashMap<>();
        for (Map.Entry<Long, Long> entry : cpuTimePerApplicationThread.entrySet()) {
            BigDecimal percentageCpuTimePerApplicationThread = new BigDecimal(entry.getValue() * 100.0 / totalApplicationCpuTime, MATH_CONTEXT);
            BigDecimal applicationThreadPower = currentPower.getValue().multiply(percentageCpuTimePerApplicationThread.divide(BigDecimal.valueOf(100), MATH_CONTEXT));
            powerPerApplicationThread.put(entry.getKey(), applicationThreadPower);
        }
        return powerPerApplicationThread;
    }
}

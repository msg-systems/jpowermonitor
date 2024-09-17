package group.msg.jpowermonitor.measurement.est;

import group.msg.jpowermonitor.config.DefaultCfgProvider;
import group.msg.jpowermonitor.config.JPowerMonitorCfgProvider;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

@Slf4j
class EstimationReaderTest {
    @BeforeEach
    void setup() {
        DefaultCfgProvider.invalidateCachedConfig();
    }

    volatile boolean threadIsStopped = false;

    @Test
    void testEstimateWattageBasedOnCpuUsage() throws ExecutionException, InterruptedException, TimeoutException {
        JPowerMonitorCfgProvider configProvider = new DefaultCfgProvider();
        configProvider.readConfig("EstimationReaderTest.yaml");
        ExecutorService executor = Executors.newSingleThreadExecutor(); // cannot use try with resources here, since we use JDK 11 for compilation.
        try {
            Callable<String> measureThread = createMeasureThread(configProvider);
            Future<String> result = executor.submit(measureThread);
            BigDecimal sum = IntStream.range(0, 100000)
                .mapToObj(x -> new BigDecimal("2.456").pow(x, new MathContext(10, RoundingMode.HALF_UP)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("Sum is {}", sum);
            threadIsStopped = true;
            String resultString = result.get(30, TimeUnit.SECONDS);
            Assertions.assertEquals("OK", resultString);
        } finally {
            executor.shutdown();
        }
    }

    private Callable<String> createMeasureThread(JPowerMonitorCfgProvider configProvider) {
        JPowerMonitorCfg config = configProvider.getCachedConfig();
        EstimationReader cmr = new EstimationReader(config);
        return () -> {
            try {
                while (!threadIsStopped) {
                    DataPoint dataPoint = cmr.measureFirstConfiguredPath();
                    log.info("cpuMeasure: " + dataPoint);
                    Assertions.assertTrue(config.getMeasurement().getEst().getCpuMinWatts() <= dataPoint.getValue()
                                          && dataPoint.getValue() <= config.getMeasurement().getEst().getCpuMaxWatts(),
                        "Value must be between configured min and max value.");
                    sleep();
                }
            } catch (AssertionError e) {
                return "Failing test:" + e.getMessage();
            }
            return "OK";
        };
    }

    private static void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("ignore InterruptedException");
        }
    }
}

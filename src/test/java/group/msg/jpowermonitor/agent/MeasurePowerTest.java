package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.WINDOWS)
@Slf4j
class MeasurePowerTest {

    private static final int REASONABLE_MEASUREMENT_INTERVAL_MS = 850;

    /**
     * Steps down wait interval (in 50ms steps) between measurements until implemented/configured MeasureMethod delivers the same value again.
     * -> e. g. for OpenHardwareMonitor between 750-850ms seems to be the minimal possible interval to get updatet values
     */
    @Test
    void findReasonableMeasurementIntervalForMeasureMethodTest() {
        long loopCount = 0;
        for (int intervalInMs = 1000; intervalInMs >= REASONABLE_MEASUREMENT_INTERVAL_MS; intervalInMs-=50) {
            log.info("Interval {}ms, loopCount {}", intervalInMs, loopCount);
            loopCount = 0;
            DataPoint dp = MeasurePower.getCurrentCpuPowerInWatts();
            log.debug("{}", dp);
            long busyWaitUntil = System.currentTimeMillis() + intervalInMs;
            while (System.currentTimeMillis() <= busyWaitUntil) {
                loopCount++;
            }
            DataPoint dp2 = MeasurePower.getCurrentCpuPowerInWatts();
            log.debug("{}", dp2);
            assertEquals(dp2.getUnit(), dp.getUnit());
            assertNotEquals(dp2.getTime(), dp.getTime());
            assertNotEquals(dp2.getValue(), dp.getValue());
        }
    }

    @Test
    void verifyReasonableMeasurementIntervalForMeasureMethodTest() throws InterruptedException {
        int intervalInMs = REASONABLE_MEASUREMENT_INTERVAL_MS;
        long loopCount = 0;
        for (int i = 0; i < 25; i++) {
            log.info("Interval {}ms, loopCount {}, run {}", intervalInMs, loopCount, i);
            loopCount = 0;
            DataPoint dp = MeasurePower.getCurrentCpuPowerInWatts();
            log.debug("{}", dp);
            long busyWaitUntil = System.currentTimeMillis() + intervalInMs;
            while (System.currentTimeMillis() <= busyWaitUntil) {
                loopCount++;
            }
            Thread.sleep(intervalInMs);
            DataPoint dp2 = MeasurePower.getCurrentCpuPowerInWatts();
            log.debug("{}", dp2);
            assertEquals(dp2.getUnit(), dp.getUnit());
            assertNotEquals(dp2.getTime(), dp.getTime());
            assertNotEquals(dp2.getValue(), dp.getValue());
        }
    }
}

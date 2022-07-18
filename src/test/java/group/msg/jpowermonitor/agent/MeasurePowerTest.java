package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnabledOnOs(OS.WINDOWS)
@Slf4j
class MeasurePowerTest {

    private static final int REASONABLE_MEASUREMENT_INTERVAL_MS = 850;

    /**
     * Steps down wait interval (in 50ms steps) between measurements until implemented/configured MeasureMethod delivers the same value again.
     * -> e. g. for OpenHardwareMonitor between 750-850ms seems to be the minimal possible interval to get updatet values
     */
    @Disabled("Use this test to find the minimum viable measurement interval for your platform and your configured measure method")
    void findReasonableMeasurementIntervalForMeasureMethodTest() {
        long loopCount = 0;
        for (int intervalInMs = 1000; intervalInMs >= REASONABLE_MEASUREMENT_INTERVAL_MS; intervalInMs -= 50) {
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
            assertThat(dp.getUnit()).isEqualTo(dp2.getUnit());
            assertThat(dp.getTime()).isNotEqualTo(dp2.getTime());
            assertThat(dp.getValue()).isNotEqualTo(dp2.getValue());
        }
    }

    @Disabled("Use this test to find the minimum viable measurement interval for your platform and your configured measure method")
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
            assertThat(dp.getUnit()).isEqualTo(dp2.getUnit());
            assertThat(dp.getTime()).isNotEqualTo(dp2.getTime());
            assertThat(dp.getValue()).isNotEqualTo(dp2.getValue());
        }
    }
}

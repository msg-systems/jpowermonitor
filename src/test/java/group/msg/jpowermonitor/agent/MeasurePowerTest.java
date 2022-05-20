package group.msg.jpowermonitor.agent;

import group.msg.jpowermonitor.dto.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
    @Test
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
            assertEquals(dp2.getUnit(), dp.getUnit());
            assertNotEquals(dp2.getTime(), dp.getTime());
            assertNotEquals(dp2.getValue(), dp.getValue());
            // FIXME Ausgabe auf meinem Rechner:
            // 12:53:09.242 [Test worker] INFO group.msg.jpowermonitor.config.JPowerMonitorConfig - No configuration name given by program arguments, using default name 'jpowermonitor.yaml'
            //12:53:09.246 [Test worker] INFO group.msg.jpowermonitor.config.JPowerMonitorConfig - No external configuration found at 'C:\ws\jpowermonitor\jpowermonitor.yaml', using configuration from classpath
            //12:53:09.542 [Test worker] DEBUG group.msg.jpowermonitor.config.JPowerMonitorConfig - Read config (including defaults): JPowerMonitorConfig(samplingIntervalInMs=300, samplingIntervalForInitInMs=1000, initCycles=10, calmDownIntervalInMs=1000, percentageOfSamplesAtBeginningToDiscard=20, openHardwareMonitor=OpenHardwareMonitor(url=http://localhost:8085/data.json, paths=[PathElement(path=[MSGN13205, Intel Core i7-9850H, Powers, CPU Package], energyInIdleMode=null)]), csvRecording=CsvRecording(resultCsv=energyconsumption.csv, measurementCsv=measurement.csv), javaAgent=JavaAgent(packageFilter=[com.msg, de.gillardon], measurementIntervalInMs=1000, gatherStatisticsIntervalInMs=10, writeEnergyMeasurementsToCsvIntervalInS=30))
            //12:53:11.223 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=7.2, unit=Unit.NONE(name=), time=2022-05-20T12:53:10.369631600)
            //12:53:12.975 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=5.9, unit=Unit.NONE(name=), time=2022-05-20T12:53:12.962747400)
            //12:53:12.987 [Test worker] INFO group.msg.jpowermonitor.agent.MeasurePowerTest - Interval 850ms, loopCount 67525643, run 1
            //12:53:12.999 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=5.9, unit=Unit.NONE(name=), time=2022-05-20T12:53:12.987680700)
            //12:53:14.721 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=5.6, unit=Unit.NONE(name=), time=2022-05-20T12:53:14.712230700)
            //12:53:14.722 [Test worker] INFO group.msg.jpowermonitor.agent.MeasurePowerTest - Interval 850ms, loopCount 73514274, run 2
            //12:53:14.731 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=5.6, unit=Unit.NONE(name=), time=2022-05-20T12:53:14.722205200)
            //12:53:16.454 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=6.7, unit=Unit.NONE(name=), time=2022-05-20T12:53:16.446010800)
            //12:53:16.455 [Test worker] INFO group.msg.jpowermonitor.agent.MeasurePowerTest - Interval 850ms, loopCount 74000315, run 3
            //12:53:16.465 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=6.7, unit=Unit.NONE(name=), time=2022-05-20T12:53:16.455984400)
            //12:53:18.188 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=6.5, unit=Unit.NONE(name=), time=2022-05-20T12:53:18.179040)
            //12:53:18.189 [Test worker] INFO group.msg.jpowermonitor.agent.MeasurePowerTest - Interval 850ms, loopCount 73438190, run 4
            //12:53:18.205 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=6.5, unit=Unit.NONE(name=), time=2022-05-20T12:53:18.189012300)
            //12:53:19.924 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=6.5, unit=Unit.NONE(name=), time=2022-05-20T12:53:19.916970500)
            //
            //expected: not equal but was: <6.5>
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

            // FIXME Ausgabe auf meinem Rechner:
            // 12:53:19.954 [Test worker] INFO group.msg.jpowermonitor.agent.MeasurePowerTest - Interval 1000ms, loopCount 0
            //12:53:19.981 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=6.5, unit=Unit.NONE(name=), time=2022-05-20T12:53:19.954870800)
            //12:53:20.991 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=7.2, unit=Unit.NONE(name=), time=2022-05-20T12:53:20.983686700)
            //12:53:20.992 [Test worker] INFO group.msg.jpowermonitor.agent.MeasurePowerTest - Interval 950ms, loopCount 81170523
            //12:53:21.000 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=7.2, unit=Unit.NONE(name=), time=2022-05-20T12:53:20.992664300)
            //12:53:21.960 [Test worker] DEBUG group.msg.jpowermonitor.agent.MeasurePowerTest - DataPoint(name=MSGN13205->Intel Core i7-9850H->Powers->CPU Package, value=7.2, unit=Unit.NONE(name=), time=2022-05-20T12:53:21.952649400)
            //
            // expected: not equal but was: <7.2>
        }
    }
}

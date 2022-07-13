package group.msg.jpowermonitor.measurement.csv;

import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.dto.DataPoint;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class CommaSeparatedValuesReaderTest {
    @Test
    void readMeasurementsFromFile() throws InterruptedException {
        JPowerMonitorConfig config = new DefaultConfigProvider().getCachedConfig();
        CommaSeparatedValuesReader cmr = new CommaSeparatedValuesReader(config); // relative path
        for (int i = 0; i < 10; i++) {
            DataPoint dataPoint = cmr.measureFirstConfiguredPath();
            System.out.println(dataPoint);
            TimeUnit.SECONDS.sleep(10);
        }
    }
}

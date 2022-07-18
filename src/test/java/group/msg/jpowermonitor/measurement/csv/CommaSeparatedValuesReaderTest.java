package group.msg.jpowermonitor.measurement.csv;

import group.msg.jpowermonitor.config.DefaultConfigProvider;
import group.msg.jpowermonitor.config.JPowerMonitorConfig;
import group.msg.jpowermonitor.config.JPowerMonitorConfigProvider;
import group.msg.jpowermonitor.dto.DataPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommaSeparatedValuesReaderTest {
    @BeforeEach
    void setup() {
        DefaultConfigProvider.invalidateCachedConfig();
    }

    @Test
    void readMeasurementsFromFile() throws InterruptedException {
        JPowerMonitorConfigProvider configProvider = new DefaultConfigProvider();
        configProvider.readConfig("CommaSeparatedValuesReaderTest.yaml");
        JPowerMonitorConfig config = configProvider.getCachedConfig();
        CommaSeparatedValuesReader cmr = new CommaSeparatedValuesReader(config);
        for (int i = 0; i < 10; i++) {
            DataPoint dataPoint = cmr.measureFirstConfiguredPath();
            System.out.println(dataPoint);
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @Test
    void readFirstLine() throws IOException {
        JPowerMonitorConfigProvider configProvider = new DefaultConfigProvider();
        configProvider.readConfig("CommaSeparatedValuesReaderTest.yaml");
        JPowerMonitorConfig config = configProvider.getCachedConfig();
        CommaSeparatedValuesReader cmr = new CommaSeparatedValuesReader(config);
        String firstLine = cmr.readFirstLine(Paths.get("src/test/resources/hwinfo-test.csv"), StandardCharsets.UTF_8);
        assertThat(firstLine).isEqualTo("12.7.2022,18:7:10.680,6.352,0.061,24.733,76.0,363,84,581.376,46.206,");
    }

    @Test
    void readUmlautsFirstLine() throws IOException {
        JPowerMonitorConfigProvider configProvider = new DefaultConfigProvider();
        configProvider.readConfig("CommaSeparatedValuesReaderTest.yaml");
        JPowerMonitorConfig config = configProvider.getCachedConfig();
        CommaSeparatedValuesReader cmr = new CommaSeparatedValuesReader(config);
        String umlauts = Files.readAllLines(Path.of("src/test/resources/umlauts.txt"), StandardCharsets.UTF_8).get(0);
        String firstLine = cmr.readFirstLine(Paths.get("src/test/resources/firstLineLastLine-test.csv"), StandardCharsets.UTF_8);
        assertThat(firstLine).isEqualTo("MyFirstLine," + umlauts + ";WithUmlauts;");
    }

    @Test
    void readLastLine() throws IOException {
        JPowerMonitorConfigProvider configProvider = new DefaultConfigProvider();
        configProvider.readConfig("CommaSeparatedValuesReaderTest.yaml");
        JPowerMonitorConfig config = configProvider.getCachedConfig();
        CommaSeparatedValuesReader cmr = new CommaSeparatedValuesReader(config);
        String lastLine = cmr.readLastLine(Paths.get("src/test/resources/hwinfo-test.csv"), StandardCharsets.UTF_8);
        assertThat(lastLine).isEqualTo("12.7.2022,18:7:16.865,7.055,0.012,25.458,75.0,367,84,603.767,47.299,");
    }

    @Test
    void readVeryLongLastLine() throws IOException {
        String umlauts = Files.readAllLines(Path.of("src/test/resources/umlauts.txt"), StandardCharsets.UTF_8).get(0);
        StringBuilder expectedSb = new StringBuilder("My_Last_Line_With_10.000_Xes_At_The_End," + umlauts + ";WithUmlauts;");
        IntStream.range(0, 10_000).forEach(i -> expectedSb.append("X"));
        JPowerMonitorConfigProvider configProvider = new DefaultConfigProvider();
        configProvider.readConfig("CommaSeparatedValuesReaderTest.yaml");
        JPowerMonitorConfig config = configProvider.getCachedConfig();
        CommaSeparatedValuesReader cmr = new CommaSeparatedValuesReader(config);
        String lastLine = cmr.readLastLine(Paths.get("src/test/resources/firstLineLastLine-test.csv"), StandardCharsets.UTF_8);
        assertThat(lastLine).isEqualTo(expectedSb.toString());
    }

}

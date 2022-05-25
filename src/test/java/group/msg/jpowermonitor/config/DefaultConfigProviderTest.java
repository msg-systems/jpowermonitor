package group.msg.jpowermonitor.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;

class DefaultConfigProviderTest {


    @Test
    public void readConfig_nullReadsDefaultConfig() {
        JPowerMonitorConfig cfg = new TestDefaultConfigProvider().readConfig(null);
        assertNotNull(cfg);
        assertEquals("test_energyconsumption.csv", cfg.getCsvRecording().getResultCsv());
    }

    @Test
    public void readConfig_nonExistingResourceReadsDefaultConfig() {
        JPowerMonitorConfig cfg = new TestDefaultConfigProvider().readConfig("iAmNotHere.yaml");
        assertNotNull(cfg);
        assertEquals("test_energyconsumption.csv", cfg.getCsvRecording().getResultCsv());
    }

    @Test
    public void readConfig_readExistingResource() {
        JPowerMonitorConfig cfg = new DefaultConfigProvider().readConfig("jpowermonitor_test.yaml");
        assertNotNull(cfg);
        assertEquals("some.test.url/data.json", cfg.getOpenHardwareMonitor().getUrl());
    }

    @Test
    public void readConfig_usesCaching() {
        JPowerMonitorConfigProvider provider = new DefaultConfigProvider();
        JPowerMonitorConfig cfg1 = provider.readConfig("jpowermonitor_test.yaml");
        assertNotNull(cfg1);
        JPowerMonitorConfig cfg2 = provider.readConfig("something.else");
        assertSame(cfg1, cfg2);
    }

    @Test
    public void readConfig_tryReadInvalidFile(@TempDir Path dir) {
        JPowerMonitorConfig cfg = new TestDefaultConfigProvider().readConfig(
            dir.toAbsolutePath().toString());
        assertNotNull(cfg);
        assertEquals("test_measurement.csv", cfg.getCsvRecording().getMeasurementCsv());
    }

    @Test
    public void readConfig_readValidExternalFile(@TempDir Path dir) throws Exception {
        Path yaml = dir.resolve("myconfig.yaml");
        Files.write(yaml, List.of(
            "!!group.msg.jpowermonitor.config.JPowerMonitorConfig",
            "openHardwareMonitor:",
            "  url: 'blablabla'",
            "  paths:",
            "    - { path: ['pc', 'cpu', 'path'], energyInIdleMode: }",
            "csvRecording:",
            "  resultCsv: 'someTempFile.csv'"), StandardCharsets.UTF_8);

        JPowerMonitorConfig cfg = new DefaultConfigProvider().readConfig(
            yaml.toAbsolutePath().toString());
        assertNotNull(cfg);
        assertEquals("someTempFile.csv", cfg.getCsvRecording().getResultCsv());
    }

    @Test
    public void readConfig_invalidResourceFallsBackToDefaul() {
        JPowerMonitorConfig cfg = new TestDefaultConfigProvider().readConfig("invalidConfig.xml");
        assertNotNull(cfg);
        assertEquals("test_energyconsumption.csv", cfg.getCsvRecording().getResultCsv());
    }

    @Test
    public void readConfig_invalidFileFallsBackToDefaul(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("invalid.txt");
        Files.write(file, List.of("not valid"), StandardCharsets.UTF_8);

        JPowerMonitorConfig cfg = new TestDefaultConfigProvider().readConfig(
            file.toAbsolutePath().toString());
        assertNotNull(cfg);
        assertEquals("test_energyconsumption.csv", cfg.getCsvRecording().getResultCsv());
    }
}

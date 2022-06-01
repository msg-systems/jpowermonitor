package group.msg.jpowermonitor.config;

import static org.junit.jupiter.api.Assertions.*;

import group.msg.jpowermonitor.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;

class DefaultConfigProviderTest {

    /*  HINT
     * Cannot really test the default behaviour for configuration loading: If we put a
     * "jpowermonitor.yaml" into working dir, it would either break the full measurement tests
     * (like EndlettLoopTest) or this test here. So without tricking the file system (like changing
     * the working directory during tests) it's not possible to fullfil all needs.
     * Thus here we skip testing the default branch of configuration finding.
     *  diehla, June 2022
     */

    @Test
    public void readConfig_fileSystemOverResource(@TempDir Path dir) throws Exception {
        Path yaml = dir.resolve("DefaultConfigProviderTest.yaml");
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
    public void readConfig_fromResourceIfNoFile() {
        JPowerMonitorConfig cfg = new DefaultConfigProvider().readConfig(
            "DefaultConfigProviderTest.yaml");
        assertNotNull(cfg);
        assertEquals("test_energyconsumption.csv", cfg.getCsvRecording().getResultCsv());
    }

    @Test
    public void readConfig_usesCaching() {
        JPowerMonitorConfigProvider provider = new DefaultConfigProvider();
        JPowerMonitorConfig cfg1 = provider.readConfig("DefaultConfigProviderTest.yaml");
        assertNotNull(cfg1);
        JPowerMonitorConfig cfg2 = provider.readConfig("something.else");
        assertSame(cfg1, cfg2);
    }

    @Test
    public void readConfig_tryReadInvalidFile(@TempDir Path dir) {
        assertThrows(
            JPowerMonitorException.class,
            () -> new DefaultConfigProvider().readConfig(dir.toAbsolutePath().toString()));
    }

    @Test
    public void readConfig_cannotReadNonYamlFile() {
        assertThrows(
            JPowerMonitorException.class,
            () -> new DefaultConfigProvider().readConfig("invalidConfig.xml"));
    }

}

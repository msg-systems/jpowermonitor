package group.msg.jpowermonitor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultConfigProviderTest {

    @BeforeEach
    void resetConfig() {
        DefaultConfigProvider.resetCachedConfig();
    }

    /*  HINT
     * Cannot really test the default behaviour for configuration loading: If we put a
     * "jpowermonitor.yaml" into working dir, it would either break the full measurement tests
     * (like EndlessLoopTest) or this test here. So without tricking the file system (like changing
     * the working directory during tests) it's not possible to fulfill all needs.
     * Thus, here we skip testing the default branch of configuration finding.
     *  diehla, June 2022
     */
    @Test
    public void readConfig_fileSystemOverResource(@TempDir Path dir) throws Exception {
        Path yaml = dir.resolve("DefaultConfigProviderTest.yaml");
        Files.write(yaml, List.of(
            "!!group.msg.jpowermonitor.config.JPowerMonitorConfig",
            "ohm:",
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

}

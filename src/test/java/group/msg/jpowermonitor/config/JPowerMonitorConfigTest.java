package group.msg.jpowermonitor.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import group.msg.jpowermonitor.JPowerMonitorException;
import org.junit.jupiter.api.*;

public class JPowerMonitorConfigTest {

    private static JPowerMonitorConfig config;
    private static final String DEFAULT_CONFIG_FOR_TEST = "jpowermonitor_test.yaml";

    @BeforeAll
    public static void readConfig() throws Throwable {
        config = JPowerMonitorConfig.readConfig(
            JPowerMonitorConfigTest.class.getSimpleName() + ".yaml", false);
    }

    @Test
    public void testFilterSet() {
        Set<String> packageFilter = config.getJavaAgent().getPackageFilter();
        assertTrue(packageFilter.contains("com.msg"));
        assertTrue(packageFilter.contains("de.gillardon"));
    }

    @Test
    public void testMeasurementInterval() {
        long measurementIntervalInMs = config.getJavaAgent().getMeasurementIntervalInMs();
        assertEquals(1000L, measurementIntervalInMs);
    }

    @Test
    public void testGatherStatisticsIntervalInMsInterval() {
        long gatherStatisticsIntervalInMs = config.getJavaAgent().getGatherStatisticsIntervalInMs();
        assertEquals(100L, gatherStatisticsIntervalInMs);
    }

    @Test
    public void testWriteEnergyMeasurementsToCsvIntervalInS() {
        long writeEnergyMeasurementsToCsvIntervalInS = config.getJavaAgent()
            .getWriteEnergyMeasurementsToCsvIntervalInS();
        assertEquals(20L, writeEnergyMeasurementsToCsvIntervalInS);
    }

    @Test
    public void readConfig_nullPath_readsDefaultConfig() {
        JPowerMonitorConfig.DEFAULT_CONFIG = DEFAULT_CONFIG_FOR_TEST;
        JPowerMonitorConfig config = JPowerMonitorConfig.readConfig(null, false);
        assertNotNull(config);
        assertEquals("some.test.url/data.json", config.getOpenHardwareMonitor().getUrl());
    }

    @Test
    public void readConfig_emptyPath_readsDefaultConfig() {
        JPowerMonitorConfig.DEFAULT_CONFIG = DEFAULT_CONFIG_FOR_TEST;
        JPowerMonitorConfig config = JPowerMonitorConfig.readConfig("", false);
        assertNotNull(config);
        assertEquals("some.test.url/data.json", config.getOpenHardwareMonitor().getUrl());
    }

    @Test
    public void readConfig_emptySpacePath_readsDefaultConfig() {
        JPowerMonitorConfig.DEFAULT_CONFIG = DEFAULT_CONFIG_FOR_TEST;
        JPowerMonitorConfig config = JPowerMonitorConfig.readConfig(" \t\n ", false);
        assertNotNull(config);
        assertEquals("some.test.url/data.json", config.getOpenHardwareMonitor().getUrl());
    }

    @Test
    public void readConfig_exceptionOnMissingConfig() {
        JPowerMonitorConfig.DEFAULT_CONFIG = "I do not exist";
        assertThrows(
            JPowerMonitorException.class,
            () -> JPowerMonitorConfig.readConfig("I do neither exist", false));
    }

    @Test
    public void readConfig_useCachingByDefault() {
        JPowerMonitorConfig cfg1 = JPowerMonitorConfig.readConfig(DEFAULT_CONFIG_FOR_TEST);
        JPowerMonitorConfig cfg2 = JPowerMonitorConfig.readConfig("jpowermonitor-2.yaml");
        assertSame(cfg1, cfg2);
    }
}

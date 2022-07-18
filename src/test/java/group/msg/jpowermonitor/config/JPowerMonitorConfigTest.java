package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.ConfigProviderForTests;
import group.msg.jpowermonitor.JPowerMonitorException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JPowerMonitorConfigTest {

    @Test
    public void initialization_noHWGroup() {
        JPowerMonitorConfig config = new JPowerMonitorConfig();
        assertThrows(JPowerMonitorException.class, config::initializeConfiguration);
    }

    @Test
    public void initialization_noUrl() {
        JPowerMonitorConfig config = new JPowerMonitorConfig();
        Measurement measurement = new Measurement();
        measurement.setMethod("ohm");
        measurement.setOhm(new OpenHardwareMonitorCfg());
        config.setMeasurement(measurement);
        assertThrows(JPowerMonitorException.class, config::initializeConfiguration);
    }

    @Test
    public void initialization_noPath() {
        OpenHardwareMonitorCfg ohmConfig = new OpenHardwareMonitorCfg();
        ohmConfig.setUrl("some.url");
        ohmConfig.setPaths(List.of(new PathElement()));
        JPowerMonitorConfig config = new JPowerMonitorConfig();
        Measurement measurement = new Measurement();
        measurement.setMethod("ohm");
        measurement.setOhm(ohmConfig);
        config.setMeasurement(measurement);
        assertThrows(JPowerMonitorException.class, config::initializeConfiguration);
    }

    @Test
    public void initialization_urlPreparation() {
        PathElement path = new PathElement();
        path.setPath(List.of("path"));
        OpenHardwareMonitorCfg ohmConfig = new OpenHardwareMonitorCfg();
        ohmConfig.setUrl("some.url");
        ohmConfig.setPaths(List.of(path));
        JPowerMonitorConfig config = new JPowerMonitorConfig();
        Measurement measurement = new Measurement();
        measurement.setMethod("ohm");
        measurement.setOhm(ohmConfig);
        config.setMeasurement(measurement);
        config.initializeConfiguration();
        assertEquals("some.url/data.json", config.getMeasurement().getOhm().getUrl());
    }

    @Test
    public void initialization_defaultValues() {
        PathElement path = new PathElement();
        path.setPath(List.of("path"));
        OpenHardwareMonitorCfg ohmConfig = new OpenHardwareMonitorCfg();
        ohmConfig.setUrl("some.url");
        ohmConfig.setPaths(List.of(path));
        JPowerMonitorConfig config = new JPowerMonitorConfig();
        Measurement measurement = new Measurement();
        measurement.setMethod("ohm");
        measurement.setOhm(ohmConfig);
        config.setMeasurement(measurement);
        config.initializeConfiguration();

        assertEquals(300, config.getSamplingIntervalInMs());
        assertEquals(1000, config.getSamplingIntervalForInitInMs());
        assertEquals(10, config.getInitCycles());
        assertEquals(1000, config.getCalmDownIntervalInMs());
        assertEquals(BigDecimal.valueOf(15), config.getPercentageOfSamplesAtBeginningToDiscard());
        assertNotNull(config.getJavaAgent());
        assertNotNull(config.getJavaAgent().getPackageFilter());
        assertTrue(config.getJavaAgent().getPackageFilter().isEmpty());
        assertEquals(0L, config.getJavaAgent().getMeasurementIntervalInMs());
        assertEquals(0L, config.getJavaAgent().getGatherStatisticsIntervalInMs());
        assertEquals(0L, config.getJavaAgent().getWriteEnergyMeasurementsToCsvIntervalInS());
    }

    @Test
    public void testFilterSet() {
        JPowerMonitorConfig config = new ConfigProviderForTests().readConfig(getClass());
        Set<String> packageFilter = config.getJavaAgent().getPackageFilter();
        assertTrue(packageFilter.contains("com.msg"));
        assertTrue(packageFilter.contains("de.gillardon"));
    }

    @Test
    public void testMeasurementInterval() {
        JPowerMonitorConfig config = new ConfigProviderForTests().readConfig(getClass());
        long measurementIntervalInMs = config.getJavaAgent().getMeasurementIntervalInMs();
        assertEquals(1000L, measurementIntervalInMs);
    }

    @Test
    public void testGatherStatisticsIntervalInMsInterval() {
        JPowerMonitorConfig config = new ConfigProviderForTests().readConfig(getClass());
        long gatherStatisticsIntervalInMs = config.getJavaAgent().getGatherStatisticsIntervalInMs();
        assertEquals(100L, gatherStatisticsIntervalInMs);
    }

    @Test
    public void testWriteEnergyMeasurementsToCsvIntervalInS() {
        JPowerMonitorConfig config = new ConfigProviderForTests().readConfig(getClass());
        long writeEnergyMeasurementsToCsvIntervalInS = config.getJavaAgent()
            .getWriteEnergyMeasurementsToCsvIntervalInS();
        assertEquals(20L, writeEnergyMeasurementsToCsvIntervalInS);
    }
}

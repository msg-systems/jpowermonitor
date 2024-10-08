package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.CfgProviderForTests;
import group.msg.jpowermonitor.JPowerMonitorException;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.config.dto.LibreHardwareMonitorCfg;
import group.msg.jpowermonitor.config.dto.MeasurementCfg;
import group.msg.jpowermonitor.config.dto.PathElementCfg;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JPowerMonitorCfgTest {

    @Test
    public void initialization_noHWGroup() {
        JPowerMonitorCfg config = new JPowerMonitorCfg();
        assertThatThrownBy(config::initializeConfiguration).isInstanceOf(JPowerMonitorException.class);
    }

    @Test
    public void initialization_noUrl() {
        JPowerMonitorCfg config = new JPowerMonitorCfg();
        MeasurementCfg measurement = new MeasurementCfg();
        measurement.setMethod("lhm");
        measurement.setLhm(new LibreHardwareMonitorCfg());
        config.setMeasurement(measurement);
        assertThatThrownBy(config::initializeConfiguration).isInstanceOf(JPowerMonitorException.class);
    }

    @Test
    public void initialization_noPath() {
        LibreHardwareMonitorCfg lhmConfig = new LibreHardwareMonitorCfg();
        lhmConfig.setUrl("some.url");
        lhmConfig.setPaths(List.of(new PathElementCfg()));
        JPowerMonitorCfg config = new JPowerMonitorCfg();
        MeasurementCfg measurement = new MeasurementCfg();
        measurement.setMethod("lhm");
        measurement.setLhm(lhmConfig);
        config.setMeasurement(measurement);
        assertThatThrownBy(config::initializeConfiguration).isInstanceOf(JPowerMonitorException.class);
    }

    @Test
    public void initialization_urlPreparation() {
        PathElementCfg path = new PathElementCfg();
        path.setPath(List.of("path"));
        LibreHardwareMonitorCfg lhmConfig = new LibreHardwareMonitorCfg();
        lhmConfig.setUrl("some.url");
        lhmConfig.setPaths(List.of(path));
        JPowerMonitorCfg config = new JPowerMonitorCfg();
        MeasurementCfg measurement = new MeasurementCfg();
        measurement.setMethod("lhm");
        measurement.setLhm(lhmConfig);
        config.setMeasurement(measurement);
        config.initializeConfiguration();
        assertThat(config.getMeasurement().getLhm().getUrl()).isEqualTo("some.url/data.json");
    }

    @Test
    public void initialization_defaultValues() {
        PathElementCfg path = new PathElementCfg();
        path.setPath(List.of("path"));
        LibreHardwareMonitorCfg lhmConfig = new LibreHardwareMonitorCfg();
        lhmConfig.setUrl("some.url");
        lhmConfig.setPaths(List.of(path));
        JPowerMonitorCfg config = new JPowerMonitorCfg();
        MeasurementCfg measurement = new MeasurementCfg();
        measurement.setMethod("lhm");
        measurement.setLhm(lhmConfig);
        config.setMeasurement(measurement);
        config.initializeConfiguration();

        assertThat(config.getSamplingIntervalInMs()).isEqualTo(300);
        assertThat(config.getSamplingIntervalForInitInMs()).isEqualTo(1000);
        assertThat(config.getInitCycles()).isEqualTo(10);
        assertThat(config.getCalmDownIntervalInMs()).isEqualTo(1000);
        assertThat(config.getPercentageOfSamplesAtBeginningToDiscard()).isEqualTo(15.0);
        assertThat(config.getJavaAgent()).isNotNull();
        assertThat(config.getJavaAgent().getPackageFilter()).isNotNull();
        assertThat(config.getJavaAgent().getPackageFilter().isEmpty()).isTrue();
        assertThat(config.getJavaAgent().getMeasurementIntervalInMs()).isEqualTo(0L);
        assertThat(config.getJavaAgent().getGatherStatisticsIntervalInMs()).isEqualTo(0L);
        assertThat(config.getJavaAgent().getWriteEnergyMeasurementsToCsvIntervalInS()).isEqualTo(0L);
    }

    @Test
    public void testFilterSet() {
        JPowerMonitorCfg config = new CfgProviderForTests().readConfig(getClass());
        Set<String> packageFilter = config.getJavaAgent().getPackageFilter();
        assertThat(packageFilter.contains("com.msg")).isTrue();
        assertThat(packageFilter.contains("de.gillardon")).isTrue();
    }

    @Test
    public void testMeasurementInterval() {
        JPowerMonitorCfg config = new CfgProviderForTests().readConfig(getClass());
        long measurementIntervalInMs = config.getJavaAgent().getMeasurementIntervalInMs();
        assertThat(measurementIntervalInMs).isEqualTo(1000L);
    }

    @Test
    public void testGatherStatisticsIntervalInMsInterval() {
        JPowerMonitorCfg config = new CfgProviderForTests().readConfig(getClass());
        long gatherStatisticsIntervalInMs = config.getJavaAgent().getGatherStatisticsIntervalInMs();
        assertThat(gatherStatisticsIntervalInMs).isEqualTo(100L);
    }

    @Test
    public void testWriteEnergyMeasurementsToCsvIntervalInS() {
        JPowerMonitorCfg config = new CfgProviderForTests().readConfig(getClass());
        long writeEnergyMeasurementsToCsvIntervalInS = config.getJavaAgent()
            .getWriteEnergyMeasurementsToCsvIntervalInS();
        assertThat(writeEnergyMeasurementsToCsvIntervalInS).isEqualTo(20L);
    }
}

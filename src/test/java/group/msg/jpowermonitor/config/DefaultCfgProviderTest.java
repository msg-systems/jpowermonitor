package group.msg.jpowermonitor.config;

import group.msg.jpowermonitor.config.dto.CsvColumnCfg;
import group.msg.jpowermonitor.config.dto.CsvMeasurementCfg;
import group.msg.jpowermonitor.config.dto.CsvRecordingCfg;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.config.dto.JavaAgentCfg;
import group.msg.jpowermonitor.config.dto.LibreHardwareMonitorCfg;
import group.msg.jpowermonitor.config.dto.MeasurementCfg;
import group.msg.jpowermonitor.config.dto.MonitoringCfg;
import group.msg.jpowermonitor.config.dto.PathElementCfg;
import group.msg.jpowermonitor.config.dto.PrometheusCfg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;


class DefaultCfgProviderTest {
    @BeforeEach
    void resetConfig() {
        DefaultCfgProvider.invalidateCachedConfig();
    }

    @Test
    public void readConfig_fromResourceIfNoFile() {
        JPowerMonitorCfg cfg = new DefaultCfgProvider().readConfig("DefaultConfigProviderTest.yaml");
        assertThat(cfg).isNotNull();

        JPowerMonitorCfg expected = new JPowerMonitorCfg();
        expected.setInitCycles(7);
        expected.setSamplingIntervalForInitInMs(8);
        expected.setCalmDownIntervalInMs(9);
        expected.setPercentageOfSamplesAtBeginningToDiscard(3.0);
        expected.setSamplingIntervalInMs(4);
        expected.setCarbonDioxideEmissionFactor(777.0);

        MeasurementCfg measurement = new MeasurementCfg();
        measurement.setMethod("lhm");

        LibreHardwareMonitorCfg lhm = new LibreHardwareMonitorCfg();
        PathElementCfg pe = new PathElementCfg();
        pe.setPath(List.of("pc", "cpu", "path1", "path2"));
        lhm.setPaths(List.of(pe));
        lhm.setUrl("some.test.url" + "/data.json"); // /data.json is internally added
        measurement.setLhm(lhm);

        CsvMeasurementCfg csv = new CsvMeasurementCfg();
        csv.setInputFile("mycsv.csv");
        csv.setLineToRead("first");
        CsvColumnCfg csvColumn = new CsvColumnCfg();
        csvColumn.setIndex(42);
        csvColumn.setName("CPU Power");
        csv.setColumns(List.of(csvColumn));
        csv.setEncoding("UTF-16");
        csv.setDelimiter(";");
        measurement.setCsv(csv);
        expected.setMeasurement(measurement);

        CsvRecordingCfg csvRecording = new CsvRecordingCfg();
        csvRecording.setMeasurementCsv("test_measurement.csv");
        csvRecording.setResultCsv("test_energyconsumption.csv");
        expected.setCsvRecording(csvRecording);

        MonitoringCfg monitoringCfg = new MonitoringCfg();
        PrometheusCfg prometheusCfg = new PrometheusCfg();
        prometheusCfg.setHttpPort(1234); // Default
        prometheusCfg.setWriteEnergyIntervalInS(30L); // Default
        monitoringCfg.setPrometheus(prometheusCfg);
        JavaAgentCfg javaAgentCfg = new JavaAgentCfg(Set.of("com.something", "com.anything"),
            2,
            3,
            4,
            monitoringCfg);
        expected.setJavaAgent(javaAgentCfg);

        assertThat(cfg).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void readConfig_usesCaching() {
        JPowerMonitorCfgProvider provider = new DefaultCfgProvider();
        JPowerMonitorCfg first = provider.readConfig("DefaultConfigProviderTest.yaml");
        assertThat(first).isNotNull();
        JPowerMonitorCfg second = provider.readConfig("something.else");
        assertSame(first, second);
    }

}

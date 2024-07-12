package group.msg.jpowermonitor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;


class DefaultConfigProviderTest {
    @BeforeEach
    void resetConfig() {
        DefaultConfigProvider.invalidateCachedConfig();
    }

    @Test
    public void readConfig_fromResourceIfNoFile() {
        JPowerMonitorConfig cfg = new DefaultConfigProvider().readConfig("DefaultConfigProviderTest.yaml");
        assertThat(cfg).isNotNull();

        JPowerMonitorConfig expected = new JPowerMonitorConfig();
        expected.setInitCycles(7);
        expected.setSamplingIntervalForInitInMs(8);
        expected.setCalmDownIntervalInMs(9);
        expected.setPercentageOfSamplesAtBeginningToDiscard(3.0);
        expected.setSamplingIntervalInMs(4);
        expected.setCarbonDioxideEmissionFactor(777.0);

        Measurement measurement = new Measurement();
        measurement.setMethod("lhm");

        LibreHardwareMonitorCfg lhm = new LibreHardwareMonitorCfg();
        PathElement pe = new PathElement();
        pe.path = List.of("pc", "cpu", "path1", "path2");
        lhm.setPaths(List.of(pe));
        lhm.setUrl("some.test.url" + "/data.json"); // /data.json is internally added
        measurement.setLhm(lhm);

        CsvMeasurementCfg csv = new CsvMeasurementCfg();
        csv.setInputFile("mycsv.csv");
        csv.setLineToRead("first");
        CsvColumn csvColumn = new CsvColumn();
        csvColumn.setIndex(42);
        csvColumn.setName("CPU Power");
        csv.setColumns(List.of(csvColumn));
        csv.setEncoding("UTF-16");
        csv.setDelimiter(";");
        measurement.setCsv(csv);
        expected.setMeasurement(measurement);

        CsvRecording csvRecording = new CsvRecording();
        csvRecording.setMeasurementCsv("test_measurement.csv");
        csvRecording.setResultCsv("test_energyconsumption.csv");
        expected.setCsvRecording(csvRecording);

        JavaAgent javaAgent = new JavaAgent();
        javaAgent.setMeasurementIntervalInMs(2);
        javaAgent.setGatherStatisticsIntervalInMs(3);
        javaAgent.setWriteEnergyMeasurementsToCsvIntervalInS(4);
        javaAgent.setPackageFilter(Set.of("com.something", "com.anything"));
        expected.setJavaAgent(javaAgent);

        assertThat(cfg).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void readConfig_usesCaching() {
        JPowerMonitorConfigProvider provider = new DefaultConfigProvider();
        JPowerMonitorConfig first = provider.readConfig("DefaultConfigProviderTest.yaml");
        assertThat(first).isNotNull();
        JPowerMonitorConfig second = provider.readConfig("something.else");
        assertSame(first, second);
    }

}

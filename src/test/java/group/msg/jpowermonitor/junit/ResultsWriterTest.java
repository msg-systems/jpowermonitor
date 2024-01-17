package group.msg.jpowermonitor.junit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ResultsWriterTest {
    private static final String NEW_LINE = System.lineSeparator();

    static Stream<Arguments> l10nTestConstructorValues() {
        return Stream.of(
            arguments(new Locale("en", "US"),
                "Time,Name,Sensor,Value,Unit,Baseload,Unit,Value+Baseload,Unit,Energy(Value),Unit,Energy(Value+Baseload),Unit,CO2 Value,Unit",
                "Time,Name,Sensor,Value,Unit"),
            arguments(new Locale("de", "DE"),
                "Uhrzeit;Name;Sensor;Wert;Einheit;Grundlast;Einheit;Wert+Grundlast;Einheit;Energie(Wert);Einheit;Energie(Wert+Grundlast);Einheit;CO2 Wert;Einheit",
                "Uhrzeit;Name;Sensor;Wert;Einheit"),
            arguments(new Locale("fr", "FR"),
                "Heure,Nom,D\u00E9tecteur,Valeur,Unit\u00E9,Grundlast,Unit\u00E9,Valeur+charge de base,Unit\u00E9,Energie(valeur),Unit\u00E9,Energie(valeur+charge de base),Unit\u00E9,CO2 Valeur,Unit\u00E9",
                "Heure,Nom,D\u00E9tecteur,Valeur,Unit\u00E9")
        );
    }

    @ParameterizedTest
    @MethodSource("l10nTestConstructorValues")
    void testConstructor(Locale currentLocale, String expContentResultCsv, String expContentMeasurementCsv) throws IOException {
        // Arrange
        Path pathToResultCsv = Paths.get("build/tmp/testResultCsvWriter/constructorResult.csv");
        Path pathToMeasurementCsv = Paths.get("build/tmp/testResultCsvWriter/constructorMeasurement.csv");
        Files.deleteIfExists(pathToResultCsv);
        Files.deleteIfExists(pathToMeasurementCsv);
        Locale.setDefault(currentLocale);
        ResultsWriter.setLocaleDependentValues();
        // Act
        new ResultsWriter(pathToResultCsv, pathToMeasurementCsv, new BigDecimal("485"));
        // Assert
        assertThat(Files.readString(pathToResultCsv, StandardCharsets.UTF_8)).isEqualTo(expContentResultCsv + NEW_LINE); // trim carriage-return
        assertThat(Files.readString(pathToMeasurementCsv, StandardCharsets.UTF_8)).isEqualTo(expContentMeasurementCsv + NEW_LINE); // trim carriage-return
    }
}

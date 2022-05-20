package group.msg.jpowermonitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class ResultCsvWriterTest {
    private static final String NEW_LINE = System.getProperty("line.separator");

    static Stream<Arguments> l10nTestConstructorValues() {
        return Stream.of(
            arguments(new Locale("en", "US"),
                "Time,Name,Sensor,Value,Unit,Baseload,Unit,Value+Baseload,Unit,Energy(Value),Unit,Energy(Value+Baseload),Unit",
                "Time,Name,Sensor,Value,Unit"),
            arguments(new Locale("de", "DE"),
                "Uhrzeit;Name;Sensor;Wert;Einheit;Grundlast;Einheit;Wert+Grundlast;Einheit;Energie(Wert);Einheit;Energie(Wert+Grundlast);Einheit",
                "Uhrzeit;Name;Sensor;Wert;Einheit"),
            arguments(new Locale("fr", "FR"),
                "Heure,Nom,D\u00E9tecteur,Valeur,Unit\u00E9,Grundlast,Unit\u00E9,Valeur+charge de base,Unit\u00E9,Energie(valeur),Unit\u00E9,Energie(valeur+charge de base),Unit\u00E9",
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
        ResultCsvWriter.setLocaleDependentValues();
        // Act
        new ResultCsvWriter(pathToResultCsv, pathToMeasurementCsv);
        // Assert
        Assertions.assertEquals(expContentResultCsv + NEW_LINE, Files.readString(pathToResultCsv, StandardCharsets.UTF_8)); // trim carriage-return
        Assertions.assertEquals(expContentMeasurementCsv + NEW_LINE, Files.readString(pathToMeasurementCsv, StandardCharsets.UTF_8)); // trim carriage-return
    }
}

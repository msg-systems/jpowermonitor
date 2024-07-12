package group.msg.jpowermonitor.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {
    @Test
    void convertJouleToWattHoursTest() {
        assertThat(Converter.convertJouleToWattHours(1.0)).isCloseTo(0.000277778, Offset.offset(0.000000001));
        assertThat(Converter.convertJouleToWattHours(1000.0)).isCloseTo(0.277778, Offset.offset(0.000001));
        assertThat(Converter.convertJouleToWattHours(3600.0)).isCloseTo(1.0, Offset.offset(0.0));
        assertThat(Converter.convertJouleToWattHours(10000000.0)).isCloseTo(2777.77, Offset.offset(0.01));
    }

    @Test
    void convertWattHoursToJouleTest() {
        assertThat(Converter.convertWattHoursToJoule(1.0)).isEqualByComparingTo(3600.0);
        assertThat(Converter.convertWattHoursToJoule(0.255)).isEqualByComparingTo(918.0);
        assertThat(Converter.convertWattHoursToJoule(0.277778)).isCloseTo(1000.0, Offset.offset(0.001));
    }

    @Test
    void convertJouleToKiloWattHoursTest() {
        assertThat(Converter.convertJouleToKiloWattHours(1.0)).isCloseTo(0.000000277778, Offset.offset(0.000000000001));
        assertThat(Converter.convertJouleToKiloWattHours(1000.0)).isCloseTo(0.000277778, Offset.offset(0.000000001));
        assertThat(Converter.convertJouleToKiloWattHours(3600.0)).isCloseTo(0.001, Offset.offset(0.001));
        assertThat(Converter.convertJouleToKiloWattHours(10000000.0)).isCloseTo(2.77, Offset.offset(0.01));
        assertThat(Converter.convertJouleToKiloWattHours(3600000)).isCloseTo(1.0, Offset.offset(0.0));
    }

    @Test
    void convertKiloWattHoursToCarbonDioxideTest() {
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(1.0, 485.0)).isCloseTo(485.0, Offset.offset(0.1));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(0.000000277778, 485.0)).isCloseTo(0.001347222, Offset.offset(0.002));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(0.1, 485.0)).isCloseTo(48.5, Offset.offset(0.1));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(0.01, 485.0)).isCloseTo(4.85, Offset.offset(0.01));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(0.01, 300.0)).isCloseTo(3.00, Offset.offset(0.01));
    }

    @Test
    void convertJouleToCarbonDioxideGramsTest() {
        assertThat(Converter.convertJouleToCarbonDioxideGrams(1.0, 485.0)).isCloseTo(0.001347222, Offset.offset(0.002));
        assertThat(Converter.convertJouleToCarbonDioxideGrams(3600000, 485.0)).isCloseTo(485.0, Offset.offset(0.1));
        assertThat(Converter.convertJouleToCarbonDioxideGrams(10000000.0, 485.0)).isCloseTo(1347.22, Offset.offset(0.01));
        assertThat(Converter.convertJouleToCarbonDioxideGrams(3600000, 400.0)).isCloseTo(400.0, Offset.offset(0.1));
    }
}

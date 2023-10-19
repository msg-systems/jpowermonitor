package group.msg.jpowermonitor.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {
    @BeforeAll
    static void setUp() {
    }

    @Test
    void convertJouleToWattHoursTest() {
        assertThat(Converter.convertJouleToWattHours(BigDecimal.valueOf(1.0))).isCloseTo(BigDecimal.valueOf(0.000277778), Offset.offset(BigDecimal.valueOf(0.000000001)));
        assertThat(Converter.convertJouleToWattHours(BigDecimal.valueOf(1000.0))).isCloseTo(BigDecimal.valueOf(0.277778), Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(Converter.convertJouleToWattHours(BigDecimal.valueOf(3600.0))).isCloseTo(BigDecimal.valueOf(1.0), Offset.offset(BigDecimal.valueOf(0.0)));
        assertThat(Converter.convertJouleToWattHours(BigDecimal.valueOf(10000000.0))).isCloseTo(BigDecimal.valueOf(2777.77), Offset.offset(BigDecimal.valueOf(0.01)));
    }

    @Test
    void convertWattHoursToJouleTest() {
        assertThat(Converter.convertWattHoursToJoule(new BigDecimal("1.0"))).isEqualByComparingTo(new BigDecimal("3600"));
        assertThat(Converter.convertWattHoursToJoule(new BigDecimal("0.255"))).isEqualByComparingTo(new BigDecimal("918"));
        assertThat(Converter.convertWattHoursToJoule(new BigDecimal("0.277778"))).isCloseTo(new BigDecimal("1000"), Offset.offset(new BigDecimal("0.001")));
    }

    @Test
    void convertJouleToKiloWattHoursTest() {
        assertThat(Converter.convertJouleToKiloWattHours(BigDecimal.valueOf(1.0))).isCloseTo(BigDecimal.valueOf(0.000000277778), Offset.offset(BigDecimal.valueOf(0.000000000001)));
        assertThat(Converter.convertJouleToKiloWattHours(BigDecimal.valueOf(1000.0))).isCloseTo(BigDecimal.valueOf(0.000277778), Offset.offset(BigDecimal.valueOf(0.000000001)));
        assertThat(Converter.convertJouleToKiloWattHours(BigDecimal.valueOf(3600.0))).isCloseTo(BigDecimal.valueOf(0.001), Offset.offset(BigDecimal.valueOf(0.001)));
        assertThat(Converter.convertJouleToKiloWattHours(BigDecimal.valueOf(10000000.0))).isCloseTo(BigDecimal.valueOf(2.77), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(Converter.convertJouleToKiloWattHours(BigDecimal.valueOf(3600000))).isCloseTo(BigDecimal.valueOf(1.0), Offset.offset(BigDecimal.valueOf(0.0)));
    }

    @Test
    void convertKiloWattHoursToCarbonDioxideTest() {
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(1.0), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(485.0), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.000000277778), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(0.001347222), Offset.offset(BigDecimal.valueOf(0.002)));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.1), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(48.5), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.01), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(4.85), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(Converter.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.01), BigDecimal.valueOf(300.0))).isCloseTo(BigDecimal.valueOf(3.00), Offset.offset(BigDecimal.valueOf(0.01)));
    }

    @Test
    void convertJouleToCarbonDioxideGramsTest() {
        assertThat(Converter.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(1.0), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(0.001347222), Offset.offset(BigDecimal.valueOf(0.002)));
        assertThat(Converter.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(3600000), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(485.0), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(Converter.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(10000000.0), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(1347.22), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(Converter.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(3600000), BigDecimal.valueOf(400.0))).isCloseTo(BigDecimal.valueOf(400.0), Offset.offset(BigDecimal.valueOf(0.1)));
    }


}

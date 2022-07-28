package group.msg.jpowermonitor.agent;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultsWriterTest {

    private static ResultsWriter rw;

    @BeforeAll
    static void setUp() {
        rw = new ResultsWriter(new PowerStatistics(0l, 0l, 123, null, null), false, BigDecimal.ZERO);
    }

    @Test
    void convertJouleToWattHoursTest() {
        assertThat(rw.convertJouleToWattHours(BigDecimal.valueOf(1.0))).isCloseTo(BigDecimal.valueOf(0.000277778), Offset.offset(BigDecimal.valueOf(0.000000001)));
        assertThat(rw.convertJouleToWattHours(BigDecimal.valueOf(1000.0))).isCloseTo(BigDecimal.valueOf(0.277778), Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(rw.convertJouleToWattHours(BigDecimal.valueOf(3600.0))).isCloseTo(BigDecimal.valueOf(1.0), Offset.offset(BigDecimal.valueOf(0.0)));
        assertThat(rw.convertJouleToWattHours(BigDecimal.valueOf(10000000.0))).isCloseTo(BigDecimal.valueOf(2777.77), Offset.offset(BigDecimal.valueOf(0.01)));
    }

    @Test
    void convertJouleToKiloWattHoursTest() {
        assertThat(rw.convertJouleToKiloWattHours(BigDecimal.valueOf(1.0))).isCloseTo(BigDecimal.valueOf(0.000000277778), Offset.offset(BigDecimal.valueOf(0.000000000001)));
        assertThat(rw.convertJouleToKiloWattHours(BigDecimal.valueOf(1000.0))).isCloseTo(BigDecimal.valueOf(0.000277778), Offset.offset(BigDecimal.valueOf(0.000000001)));
        assertThat(rw.convertJouleToKiloWattHours(BigDecimal.valueOf(3600.0))).isCloseTo(BigDecimal.valueOf(0.001), Offset.offset(BigDecimal.valueOf(0.001)));
        assertThat(rw.convertJouleToKiloWattHours(BigDecimal.valueOf(10000000.0))).isCloseTo(BigDecimal.valueOf(2.77), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(rw.convertJouleToKiloWattHours(BigDecimal.valueOf(3600000))).isCloseTo(BigDecimal.valueOf(1.0), Offset.offset(BigDecimal.valueOf(0.0)));
    }

    void convertKiloWattHoursToCarbonDioxideTest() {
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(1.0), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(485.0), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.000000277778), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(0.001347222), Offset.offset(BigDecimal.valueOf(0.000000000001)));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.1), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(48.5), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.01), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(4.85), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(BigDecimal.valueOf(0.01), BigDecimal.valueOf(300.0))).isCloseTo(BigDecimal.valueOf(3.00), Offset.offset(BigDecimal.valueOf(0.01)));
    }

    void convertJouleToCarbonDioxideGramsTest() {
        assertThat(rw.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(1.0), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(0.001347222), Offset.offset(BigDecimal.valueOf(0.000000000001)));
        assertThat(rw.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(3600000), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(485.0), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(rw.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(10000000.0), BigDecimal.valueOf(485.0))).isCloseTo(BigDecimal.valueOf(1343.45), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(rw.convertJouleToCarbonDioxideGrams(BigDecimal.valueOf(3600000), BigDecimal.valueOf(400.0))).isCloseTo(BigDecimal.valueOf(400.0), Offset.offset(BigDecimal.valueOf(0.1)));
    }
}

package group.msg.jpowermonitor.agent;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultsWriterTest {

    private static ResultsWriter rw;

    @BeforeAll
    static void setUp() {
        rw = new ResultsWriter(new PowerStatistics(0l, 0l, 123, null, null), false);
    }

    @Test
    void convertJouleToWattHoursTest() {
        assertThat(rw.convertJouleToWattHours(1.0)).isEqualTo(0.000277778, Offset.offset(0.000000001));
        assertThat(rw.convertJouleToWattHours(1000.0)).isEqualTo(0.277778, Offset.offset(0.000001));
        assertThat(rw.convertJouleToWattHours(3600.0)).isEqualTo(1.0, Offset.offset(0.0));
        assertThat(rw.convertJouleToWattHours(10000000.0)).isEqualTo(2777.77, Offset.offset(0.01));
    }

    @Test
    void convertJouleToKiloWattHoursTest() {
        assertThat(rw.convertJouleToKiloWattHours(1.0)).isEqualTo(0.000000277778, Offset.offset(0.000000000001));
        assertThat(rw.convertJouleToKiloWattHours(1000.0)).isEqualTo(0.000277778, Offset.offset(0.000000001));
        assertThat(rw.convertJouleToKiloWattHours(3600.0)).isEqualTo(0.001, Offset.offset(0.001));
        assertThat(rw.convertJouleToKiloWattHours(10000000.0)).isEqualTo(2.77, Offset.offset(0.01));
        assertThat(rw.convertJouleToKiloWattHours(3600000)).isEqualTo(1.0, Offset.offset(0.0));
    }

    void convertKiloWattHoursToCarbonDioxideTest() {
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(1.0, 485.0)).isEqualTo(485.0, Offset.offset(0.1));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(0.000000277778, 485.0)).isEqualTo(0.001347222, Offset.offset(0.000000000001));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(0.1, 485.0)).isEqualTo(48.5, Offset.offset(0.1));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(0.01, 485.0)).isEqualTo(4.85, Offset.offset(0.01));
        assertThat(rw.convertKiloWattHoursToCarbonDioxideGrams(0.01, 300.0)).isEqualTo(3.00, Offset.offset(0.01));
    }

    void convertJouleToCarbonDioxideGramsTest() {
        assertThat(rw.convertJouleToCarbonDioxideGrams(1.0, 485.0)).isEqualTo(0.001347222, Offset.offset(0.000000000001));
        assertThat(rw.convertJouleToCarbonDioxideGrams(3600000, 485.0)).isEqualTo(485.0, Offset.offset(0.1));
        assertThat(rw.convertJouleToCarbonDioxideGrams(10000000.0, 485.0)).isEqualTo(1343.45, Offset.offset(0.01));
        assertThat(rw.convertJouleToCarbonDioxideGrams(3600000, 400.0)).isEqualTo(400.0, Offset.offset(0.1));
    }
}

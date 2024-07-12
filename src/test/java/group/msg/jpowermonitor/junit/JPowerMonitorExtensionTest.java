package group.msg.jpowermonitor.junit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class JPowerMonitorExtensionTest {

    @ParameterizedTest
    @CsvSource({
        "0,0,0",
        "100,10,10",
        "100,100,100",
        "100,-1,0"
    })
    void firstXPercent(int listSize, double percentage, int expectedResult) {
        assertThat(new JPowerMonitorExtension().firstXPercent(listSize, percentage)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
        "0.9988, 1.00",
        "2.355, 2.36",
        "2.354, 2.35",
    })
    void roundScale2(double toRound, double rounded) {
        assertThat(new JPowerMonitorExtension().roundScale2(toRound)).isEqualTo(rounded);
    }

    @ParameterizedTest
    @CsvSource({
        "0.9233988, 0.9234",
        "2.323355, 2.32335", // 2.323355 * 100000 = 232335.49999999997 we accept this minor inaccuracy for the sake of performance when rounding!
        "2.323354, 2.32335",
    })
    void roundScale5(double toRound, double rounded) {
        assertThat(new JPowerMonitorExtension().roundScale5(toRound)).isEqualTo(rounded);
    }
}

package group.msg.jpowermonitor.junit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JPowerMonitorExtensionTest {

    @ParameterizedTest
    @CsvSource({
        "0,0,0",
        "100,10,10",
        "100,100,100",
        "100,-1,0"
    })
    void firstXPercent(int listSize, BigDecimal percentage, int expectedResult) {
        assertThat(new JPowerMonitorExtension().firstXPercent(listSize, percentage)).isEqualTo(expectedResult);
    }
}

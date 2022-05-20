package group.msg.jpowermonitor.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class SensorValue implements PowerQuestionable {
    String name;
    BigDecimal value;
    String unit;
    BigDecimal powerInIdleMode;
    LocalDateTime executionTime;
    long durationOfTestInNanoSeconds;
    BigDecimal valueWithoutIdlePowerPerHour;
    BigDecimal valueWithIdlePowerPerHour;
}

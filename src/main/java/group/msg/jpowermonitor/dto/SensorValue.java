package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class SensorValue implements PowerQuestionable {
    String name;
    BigDecimal value;
    Unit unit;
    BigDecimal powerInIdleMode;
    LocalDateTime executionTime;
    long durationOfTestInNanoSeconds;
    BigDecimal valueWithoutIdlePowerPerHour;
    BigDecimal valueWithIdlePowerPerHour;
}

package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * SensorValue that is measured by external tool.
 */
@Value
@Builder
public class SensorValue implements PowerQuestionable {
    String name;
    Double value;
    Unit unit;
    Double powerInIdleMode;
    LocalDateTime executionTime;
    long durationOfTestInNanoSeconds;
    Double valueWithoutIdlePowerPerHour;
    Double valueWithIdlePowerPerHour;
}

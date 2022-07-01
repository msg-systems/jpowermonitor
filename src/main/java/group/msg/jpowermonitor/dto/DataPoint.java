package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class DataPoint implements PowerQuestionable {
    String name;
    BigDecimal value;
    Unit unit;
    LocalDateTime time;
    Long threadId;
}

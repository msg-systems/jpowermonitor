package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * One data point.
 */
@Value
public class DataPoint implements PowerQuestionable {
    String name;
    Double value;
    Unit unit;
    LocalDateTime time;
    String threadName;
}

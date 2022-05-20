package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class Quantity {
    BigDecimal value;
    Unit unit;
}

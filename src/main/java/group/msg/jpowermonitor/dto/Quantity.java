package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Value;

import java.math.BigDecimal;

/**
 * An amount of a {@link Unit}
 */
@Value
public class Quantity {
    BigDecimal value;
    Unit unit;

    public static Quantity of(BigDecimal value, Unit unit) {
        return new Quantity(value, unit);
    }
}

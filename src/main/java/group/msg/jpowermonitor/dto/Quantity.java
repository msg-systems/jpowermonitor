package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import lombok.Value;

/**
 * An amount of a {@link Unit}
 */
@Value
public class Quantity {
    Double value;
    Unit unit;

    public static Quantity of(Double value, Unit unit) {
        return new Quantity(value, unit);
    }
}

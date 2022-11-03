package group.msg.jpowermonitor.agent;

import lombok.Getter;

/**
 * Unit of a sensor value.
 *
 * @see group.msg.jpowermonitor.dto.Quantity
 * @see group.msg.jpowermonitor.dto.DataPoint
 * @see group.msg.jpowermonitor.dto.PowerQuestionable
 */
public enum Unit {
    JOULE("J"), WATT("W"), WATTHOURS("Wh"), NONE("");
    @Getter
    private final String abbreviation;

    Unit(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    public String toString() {
        return abbreviation;
    }

    public static Unit fromAbbreviation(String abbreviation) {
        if (abbreviation == null) {
            return Unit.NONE;
        }
        switch (abbreviation) {
            case "J":
                return Unit.JOULE;
            case "W":
                return Unit.WATT;
            case "Wh":
                return Unit.WATTHOURS;
            default:
                return Unit.NONE;
        }
    }

}

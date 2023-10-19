package group.msg.jpowermonitor.agent;

import lombok.Getter;

/**
 * Units used in jPowerMonitor (sensor values and outputs).
 *
 * @see group.msg.jpowermonitor.dto.Quantity
 * @see group.msg.jpowermonitor.dto.DataPoint
 * @see group.msg.jpowermonitor.dto.PowerQuestionable
 */
@Getter
public enum Unit {
    JOULE("J"), WATT("W"), WATTHOURS("Wh"), KILOWATTHOURS("kWh"), GRAMS_CO2("gCO2"), NONE("");
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
            case "kWh":
                return Unit.KILOWATTHOURS;
            case "gCO2":
                return Unit.GRAMS_CO2;
            default:
                return Unit.NONE;
        }
    }

}

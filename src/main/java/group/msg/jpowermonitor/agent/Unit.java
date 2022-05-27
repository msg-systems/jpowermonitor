package group.msg.jpowermonitor.agent;

import lombok.Getter;
import java.util.Arrays;

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
        try {
            return Arrays.stream(Unit.values()).filter(u -> u.getAbbreviation().equals(abbreviation)).findFirst().orElse(Unit.NONE);
        } catch (Exception e) {
            return Unit.NONE;
        }
    }

}

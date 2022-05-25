package group.msg.jpowermonitor.agent;

import lombok.Getter;
import java.util.Arrays;

public enum Unit {
    JOULE("J"), WATT("W"), WATTHOURS("Wh"), NONE("");
    @Getter
    private final String name;

    Unit(String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }

    public static Unit fromName(String name) {
        try {
            return Arrays.stream(Unit.values()).filter(u -> u.getName().equals(name)).findFirst().orElse(Unit.NONE);
        } catch (Exception e) {
            return Unit.NONE;
        }
    }

}

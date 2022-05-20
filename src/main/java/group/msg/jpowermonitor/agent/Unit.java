package group.msg.jpowermonitor.agent;

import lombok.Getter;
import lombok.ToString;

@ToString(of = {"name"})
public enum Unit {
    JOULE("J"), WATT("W"), WATTHOURS("Wh"), NONE("");
    @Getter
    private final String name;

    Unit(String name) {
        this.name = name;
    }

    public static Unit fromValue(String val) {
        try {
            return valueOf(val);
        } catch (Exception e) {
            return Unit.NONE;
        }
    }
}

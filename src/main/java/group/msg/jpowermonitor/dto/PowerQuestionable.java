package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;

public interface PowerQuestionable {
    Unit getUnit();

    default boolean isPowerSensor() {
        return Unit.WATT.equals(getUnit());
    }
}

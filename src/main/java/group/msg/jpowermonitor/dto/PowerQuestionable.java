package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;

/**
 * Interface to find out unit of sensor and if it is a power sensor.
 */
public interface PowerQuestionable {
    Unit getUnit();

    default boolean isPowerSensor() {
        return Unit.WATT.equals(getUnit());
    }
}

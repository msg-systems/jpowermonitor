package group.msg.jpowermonitor.dto;

public interface PowerQuestionable {
    String getUnit();

    default boolean isPowerSensor() {
        return "W".equals(getUnit());
    }
}

package group.msg.jpowermonitor.dto;

import group.msg.jpowermonitor.agent.Unit;
import group.msg.jpowermonitor.config.dto.JPowerMonitorCfg;
import group.msg.jpowermonitor.util.Converter;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * One data point. In case of energy data point, contains the co2 representation of the value, too.
 */
@Value
public class DataPoint implements PowerQuestionable {
    String name;
    Double value;
    Unit unit;
    LocalDateTime time;
    String threadName;

    /**
     * The CO2 value in grams, only != null, if the Unit is WATT (energy value).
     */
    Double co2Value;

    public DataPoint(String name, Double value, Unit unit, LocalDateTime time, String threadName) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.time = time;
        this.threadName = threadName;
        if (Unit.JOULE.equals(unit)) {
            co2Value = Converter.convertJouleToCarbonDioxideGrams(value, JPowerMonitorCfg.getCo2EmissionFactor());
        } else {
            co2Value = null;
        }
    }
}

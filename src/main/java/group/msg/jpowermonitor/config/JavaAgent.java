package group.msg.jpowermonitor.config;

import lombok.Data;

import java.util.Set;

/**
 * Data class for java agent config.
 */
@Data
public class JavaAgent {
    private Set<String> packageFilter;
    private long measurementIntervalInMs;
    private long gatherStatisticsIntervalInMs;
    private long writeEnergyMeasurementsToCsvIntervalInS;
}

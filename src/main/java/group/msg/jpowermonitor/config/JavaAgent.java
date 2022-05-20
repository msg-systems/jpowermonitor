package group.msg.jpowermonitor.config;

import lombok.Data;

import java.util.Set;

@Data
public class JavaAgent {
    private Set<String> packageFilter;
    private long measurementIntervalInMs;
    private long gatherStatisticsIntervalInMs;
    private long writeEnergyMeasurementsToCsvIntervalInS;
}

package group.msg.jpowermonitor.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

/**
 * Data class for java agent config.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaAgentCfg {
    private Set<String> packageFilter = Collections.emptySet();
    private long measurementIntervalInMs;
    private long gatherStatisticsIntervalInMs;
    private long writeEnergyMeasurementsToCsvIntervalInS;
    private MonitoringCfg monitoring = new MonitoringCfg();
}

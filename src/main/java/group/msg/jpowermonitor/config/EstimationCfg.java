package group.msg.jpowermonitor.config;

import lombok.Data;

/**
 * Data class for estimation measurement config.
 */
@Data
public class EstimationCfg {
    private float cpuMinWatts;
    private float cpuMaxWatts;
}

package group.msg.jpowermonitor.config.dto;

import lombok.Data;

/**
 * Data class for estimation measurement config.
 */
@Data
public class EstimationCfg {
    private Double cpuMinWatts;
    private Double cpuMaxWatts;
}

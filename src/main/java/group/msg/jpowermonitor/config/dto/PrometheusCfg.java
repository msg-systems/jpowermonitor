package group.msg.jpowermonitor.config.dto;

import lombok.Data;

/**
 * Data element for prometheus config.
 */
@Data
public class PrometheusCfg {
    boolean enabled; // Default: false
    Integer httpPort;
    Long writeEnergyIntervalInS;
    boolean publishJvmMetrics; // Default: false
}

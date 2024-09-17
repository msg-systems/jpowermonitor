package group.msg.jpowermonitor.config.dto;

import lombok.Data;

/**
 * Data element for monitoring config.
 */
@Data
public class MonitoringCfg {
    PrometheusCfg prometheus = new PrometheusCfg();
}

package group.msg.jpowermonitor.config.dto;

import lombok.Data;

import java.util.List;

/**
 * Data element for Libre Hardware Monitor config.
 *
 * @see PathElementCfg
 */
@Data
public class LibreHardwareMonitorCfg {
    private String url;
    private List<PathElementCfg> paths;
}

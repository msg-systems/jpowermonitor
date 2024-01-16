package group.msg.jpowermonitor.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Data element for Libre Hardware Monitor config.
 *
 * @see PathElement
 */
@Data
public class LibreHardwareMonitorCfg {
    private String url;
    private List<PathElement> paths;
}

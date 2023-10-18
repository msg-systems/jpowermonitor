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
    @Nullable
    private String url;
    @Nullable
    private List<PathElement> paths;
}

package group.msg.jpowermonitor.config.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Data class for path element for Libre Hardware Monitor path.
 */
@Data
public class PathElementCfg {
    List<String> path;
    @Nullable
    Double energyInIdleMode;
}

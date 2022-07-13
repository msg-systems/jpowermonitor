package group.msg.jpowermonitor.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class OpenHardwareMonitorCfg {
    @Nullable
    private String url;
    @Nullable
    private List<PathElement> paths;
}

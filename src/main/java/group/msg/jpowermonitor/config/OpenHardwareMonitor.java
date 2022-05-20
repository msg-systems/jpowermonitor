package group.msg.jpowermonitor.config;

import lombok.Data;

import java.util.List;

@Data
public class OpenHardwareMonitor {
    private String url;
    private List<PathElement> paths;
}

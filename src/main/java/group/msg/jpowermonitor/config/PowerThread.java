package group.msg.jpowermonitor.config;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PowerThread {
    private long threadId;
    private List<Method> methods;
}

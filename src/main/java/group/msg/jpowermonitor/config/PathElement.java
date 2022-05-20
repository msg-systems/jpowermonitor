package group.msg.jpowermonitor.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PathElement {
    List<String> path;
    @Nullable
    BigDecimal energyInIdleMode;
}

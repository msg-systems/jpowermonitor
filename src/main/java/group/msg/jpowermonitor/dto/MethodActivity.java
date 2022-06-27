package group.msg.jpowermonitor.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Data
public class MethodActivity implements Activity {
    Long processID;
    LocalDateTime time;
    String methodQualifier;
    String filteredMethodQualifier;
    Quantity representedQuantity;

    @Override
    public @NotNull String getIdentifier(boolean asFiltered) {
        return asFiltered || methodQualifier == null ? filteredMethodQualifier : methodQualifier;
    }

    @Override
    public boolean isFinalized() {
        return representedQuantity != null;
    }
}

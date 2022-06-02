package group.msg.jpowermonitor.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
public class MethodActivity implements Activity {
    Long threadId;
    LocalDateTime time;
    String methodQualifier;
    String filteredMethodQualifier;
    Quantity representedQuantity;

    @Override
    public String getIdentifier(boolean asFiltered) {
        return Optional.ofNullable(methodQualifier)
            .filter(qualifier -> !asFiltered)
            .orElseGet(() -> Optional.ofNullable(filteredMethodQualifier).orElse(methodQualifier));
    }

    @Override
    public boolean isFinalized() {
        return representedQuantity != null;
    }
}

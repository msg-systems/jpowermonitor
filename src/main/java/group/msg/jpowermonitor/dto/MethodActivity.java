package group.msg.jpowermonitor.dto;

import lombok.Data;

import java.util.Optional;

@Data
public class MethodActivity implements Activity {
    String methodQualifier;
    String filteredMethodQualifier;
    Quantity representedQuantity;

    @Override
    public String getIdentifier(boolean asFiltered) {
        return Optional.ofNullable(methodQualifier)
            .filter(qualifier -> !asFiltered)
            .orElse(filteredMethodQualifier);
    }

    @Override
    public boolean isFinalized() {
        return representedQuantity != null;
    }
}

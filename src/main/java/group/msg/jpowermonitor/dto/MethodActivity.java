package group.msg.jpowermonitor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MethodActivity implements Activity {
    String threadName;
    LocalDateTime time;
    String methodQualifier;
    String filteredMethodQualifier;
    Quantity representedQuantity;

    @Override
    public String getIdentifier(boolean asFiltered) {
        return asFiltered || methodQualifier == null ? filteredMethodQualifier : methodQualifier;
    }

    @Override
    public boolean isFinalized() {
        return representedQuantity != null;
    }
}

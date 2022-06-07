package group.msg.jpowermonitor.dto;

import java.time.LocalDateTime;

public interface Activity {
    Long getThreadId();
    LocalDateTime getTime();
    String getIdentifier(boolean asFiltered);
    Quantity getRepresentedQuantity();
    boolean isFinalized();
}

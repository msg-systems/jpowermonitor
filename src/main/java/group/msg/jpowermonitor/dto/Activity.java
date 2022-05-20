package group.msg.jpowermonitor.dto;

public interface Activity {
    String getIdentifier(boolean asFiltered);
    Quantity getRepresentedQuantity();
    boolean isFinalized();
}

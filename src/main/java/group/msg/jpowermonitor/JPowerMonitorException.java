package group.msg.jpowermonitor;

public class JPowerMonitorException extends RuntimeException {
    public JPowerMonitorException(String message) {
        super(message);
    }

    public JPowerMonitorException(String message, Throwable cause) {
        super(message, cause);
    }
}

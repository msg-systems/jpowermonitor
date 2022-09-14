package group.msg.jpowermonitor;

/**
 * Exception that may occur in jpower monitor during reading configuration or during measuring.
 */
public class JPowerMonitorException extends RuntimeException {
    public JPowerMonitorException(String message) {
        super(message);
    }

    public JPowerMonitorException(String message, Throwable cause) {
        super(message, cause);
    }
}

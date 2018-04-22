package metaengine;

public class InvalidConfigurationException extends Exception {
    public InvalidConfigurationException() { }
    public InvalidConfigurationException(String message) {
        super(message);
    }
    public InvalidConfigurationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

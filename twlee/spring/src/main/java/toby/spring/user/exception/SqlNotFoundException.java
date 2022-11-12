package toby.spring.user.exception;

public class SqlNotFoundException extends RuntimeException {
    public SqlNotFoundException(String message) {
        super(message);
    }

    public SqlNotFoundException(Throwable cause) {
        super(cause);
    }

    public SqlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

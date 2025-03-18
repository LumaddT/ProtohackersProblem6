package speed.daemon.exceptions;

import java.io.IOException;

public class ExpectedMoreBytesException extends IOException {
    public ExpectedMoreBytesException(String message) {
        super(message);
    }
}

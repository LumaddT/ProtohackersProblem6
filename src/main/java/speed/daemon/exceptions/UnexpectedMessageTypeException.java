package speed.daemon.exceptions;

import java.io.IOException;

public class UnexpectedMessageTypeException extends IOException {
    public UnexpectedMessageTypeException(String message) {
        super(message);
    }
}

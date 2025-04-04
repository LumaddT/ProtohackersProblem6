package speed.daemon.exceptions;

public class SocketIsDeadException extends Exception {
    public SocketIsDeadException(String message) {
        super(message);
    }
}

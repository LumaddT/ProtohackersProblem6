package speed.daemon.devices;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.clientMessages.ClientMessage;
import speed.daemon.clientMessages.Plate;
import speed.daemon.codex.SocketHolder;
import speed.daemon.exceptions.SocketIsDeadException;

@RequiredArgsConstructor
public class Camera {
    private static final Logger logger = LogManager.getLogger();

    private final SocketHolder SocketHolder;
    private final int Road;
    private final int Mile;

    public void run() {
        ClientMessage clientMessage = null;
        try {
            clientMessage = SocketHolder.getNextClientMessage();
        } catch (SocketIsDeadException e) {
            this.disconnect();
        }

        Plate plate = (Plate) clientMessage;

        // TODO
    }

    public void disconnect() {
        SocketHolder.close();

        // TODO: clean from objects holder
    }
}

package speed.daemon.devices;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.MessageTypes;
import speed.daemon.clientMessages.ClientMessage;
import speed.daemon.clientMessages.IAmCamera;
import speed.daemon.clientMessages.Plate;
import speed.daemon.codex.SocketHolder;
import speed.daemon.exceptions.SocketIsDeadException;
import speed.daemon.serverMessages.Error;

@Getter
public class Camera {
    private static final Logger logger = LogManager.getLogger();

    private final SocketHolder SocketHolder;
    private final int Road;
    private final int Mile;
    private final int Limit;

    public Camera(SocketHolder socketHolder) {
        if (socketHolder.getInitialMessage().getMessageType() != MessageTypes.I_AM_CAMERA) {
            throw new RuntimeException("SocketHolder with initial message type %s sent to Camera constructor. This should never happen."
                    .formatted(socketHolder.getInitialMessage().getMessageType().name()));
        }

        IAmCamera initialMessage = (IAmCamera) socketHolder.getInitialMessage();

        SocketHolder = socketHolder;
        Road = initialMessage.getRoad();
        Mile = initialMessage.getMile();
        Limit = initialMessage.getLimit();
    }

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

    public void sendError(Error.ErrorTypes errorType) {
        SocketHolder.sendError(errorType);
    }

    public void disconnect() {
        SocketHolder.close();
    }
}

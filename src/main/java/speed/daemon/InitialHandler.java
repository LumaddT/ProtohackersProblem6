package speed.daemon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.clientMessages.ClientMessage;

import java.net.Socket;

// TODO: the cameras and dispatchers set within the maps are not concurrent
// and they are used with no synchronization in mind, hoping this will work on its own
public class InitialHandler {
    private static final Logger logger = LogManager.getLogger();

    public static void manageSocket(Socket socket) {
        SocketHolder socketHolder = new SocketHolder(socket);

        if (!socketHolder.isConnectionAlive()) {
            logger.debug("Client socket died before any message could be received.");
            return;
        }

        ClientMessage initialMessage = socketHolder.getInitialMessage();

        switch (initialMessage.getMessageType()) {
            case I_AM_CAMERA -> IslandManager.addCamera(socketHolder);
            case I_AM_DISPATCHER -> IslandManager.addDispatcher(socketHolder);
            default ->
                    logger.warn("Received illegal message {} as first message from socket {}.", initialMessage.toString(), socketHolder.hashCode());
        }
    }
}

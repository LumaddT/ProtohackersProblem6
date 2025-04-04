package speed.daemon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.clientMessages.ClientMessage;
import speed.daemon.clientMessages.IAmCamera;
import speed.daemon.clientMessages.IAmDispatcher;
import speed.daemon.codex.SocketHolder;
import speed.daemon.devices.Camera;
import speed.daemon.devices.Dispatcher;

import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// TODO: the cameras and dispatchers set within the maps are not concurrent
// and they are used with no synchronization in mind, hoping this will work on its own
public class InitialHandler {
    private static final Logger logger = LogManager.getLogger();

    public static void manageSocket(Socket socket) {
        SocketHolder socketHolder = new SocketHolder(socket);

        if (!socketHolder.isConnectionAlive()) {
            logger.debug("Client socket died before any message could be received.");
        }

        ClientMessage initialMessage = socketHolder.getInitialMessage();

        logger.info("Received {} as first message from socket.", initialMessage.toString());

        switch (initialMessage.getMessageType()) {
            case I_AM_CAMERA -> IslandManager.addCamera(socketHolder);
            case I_AM_DISPATCHER -> IslandManager.addDispatcher(socketHolder);
        }
    }
}

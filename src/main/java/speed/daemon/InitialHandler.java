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

    private static final Map<Integer, Set<Camera>> Cameras = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Dispatcher>> Dispatchers = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> Limits = new ConcurrentHashMap<>();
    private static final Map<String, Car> Cars = new ConcurrentHashMap<>();

    public static void manageSocket(Socket socket) {
        SocketHolder socketHolder = new SocketHolder(socket);

        if (!socketHolder.isConnectionAlive()) {
            logger.debug("Client socket died before any message could be received.");
        }

        ClientMessage initialMessage = socketHolder.getInitialMessage();

        logger.info("Received {} as first message from socket.", initialMessage.toString());

        switch (initialMessage.getMessageType()) {
            case I_AM_CAMERA -> {
                IAmCamera iAmCamera = (IAmCamera) initialMessage;
                Camera newCamera = new Camera(socketHolder, iAmCamera.getRoad(), iAmCamera.getMile());

                if (!Cameras.containsKey(iAmCamera.getRoad())) {
                    Cameras.put(iAmCamera.getRoad(), new HashSet<>());
                }

                Cameras.get(iAmCamera.getRoad()).add(newCamera);
            }
            case I_AM_DISPATCHER -> {
                IAmDispatcher iAmDispatcher = (IAmDispatcher) initialMessage;
                Dispatcher newDispatcher = new Dispatcher(socketHolder);

                for (int road : (iAmDispatcher.getRoads())) {
                    if (!Dispatchers.containsKey(road)) {
                        Dispatchers.put(road, new HashSet<>());
                    }

                    Dispatchers.get(road).add(newDispatcher);
                }
            }
        }
    }

    public static void reportPlate(String plateNumber, int road, long timestamp, int mile) {
        if (!Cars.containsKey(plateNumber)) {
            Cars.put(plateNumber, new Car(plateNumber));
        }
    }

    public static void disconnectAll() {
        for (Camera camera : Cameras.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            camera.disconnect();
        }

        for (Dispatcher dispatcher : Dispatchers.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            dispatcher.disconnect();
        }
    }
}

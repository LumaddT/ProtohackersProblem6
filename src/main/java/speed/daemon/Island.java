package speed.daemon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.clientMessages.IAmCamera;
import speed.daemon.clientMessages.IAmDispatcher;
import speed.daemon.codex.MessageDecoder;
import speed.daemon.devices.Camera;
import speed.daemon.devices.Dispatcher;
import speed.daemon.exceptions.ExpectedMoreBytesException;
import speed.daemon.exceptions.UnexpectedMessageTypeException;
import speed.daemon.serverMessages.Error;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// TODO: the cameras and dispatchers set within the maps are not concurrent
// and they are used with no synchronization in mind, hoping this will work on its own
class Island {
    private static final Logger logger = LogManager.getLogger();

    private final Map<Integer, Set<Camera>> Cameras = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Dispatcher>> Dispatchers = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> Limits = new ConcurrentHashMap<>();
    private final Map<String, Car> Cars = new ConcurrentHashMap<>();

    public void manageSocket(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            int firstByte = inputStream.read();

            MessageTypes messageType = MessageTypes.getType(firstByte);
            switch (messageType) {
                case I_AM_CAMERA -> {
                    IAmCamera clientMessage = MessageDecoder.parseIAmCamera(inputStream);
                    Limits.putIfAbsent(clientMessage.getRoad(), clientMessage.getLimit());

                    if (!Cameras.containsKey(clientMessage.getRoad())) {
                        Cameras.put(clientMessage.getRoad(), new HashSet<>());
                    }

                    Cameras.get(clientMessage.getRoad()).add(new Camera(socket, clientMessage.getMile()));
                }
                case I_AM_DISPATCHER -> {
                    IAmDispatcher clientMessage = MessageDecoder.parseIAmDispatcher(inputStream);
                    Dispatcher newDispatcher = new Dispatcher(socket);

                    for (int roadNumber : clientMessage.getRoads()) {
                        if (!Dispatchers.containsKey(roadNumber)) {
                            Dispatchers.put(roadNumber, new HashSet<>());
                        }

                        Dispatchers.get(roadNumber).add(newDispatcher);
                    }
                }
                default ->
                        throw new UnexpectedMessageTypeException("Expected I_AM_CAMERA or I_AM_DISPATCHER but found %s.".formatted(messageType.name()));
            }
        } catch (UnexpectedMessageTypeException e) {
            Error errorMessage = new Error(Error.ErrorTypes.UNEXPECTED_MESSAGE_TYPE);
            int[] encodedErrorMessage = errorMessage.encode();
            socket.getOutputStream().write(encodedErrorMessage, 0, encodedErrorMessage.length);
        } catch (ExpectedMoreBytesException e) {
        } catch (IOException e) {
            logger.error("An IO exception was thrown while reading the first byte of an incoming connection.\n{}\n{}", e.getMessage(), e.getStackTrace());
            try {
                socket.close();
            } catch (IOException ex) {
                logger.error("An IO exception was thrown while closing a socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    public void disconnectAll() {
        for (Camera camera : Cameras.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            camera.disconnect();
        }

        for (Dispatcher dispatcher : Dispatchers.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            dispatcher.disconnect();
        }
    }
}

package speed.daemon;

import speed.daemon.codex.SocketHolder;
import speed.daemon.devices.Camera;
import speed.daemon.devices.Dispatcher;
import speed.daemon.serverMessages.Error;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IslandManager {
    private static final Map<Integer, Set<Camera>> Cameras = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Dispatcher>> Dispatchers = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> Limits = new ConcurrentHashMap<>();
    private static final Map<String, Car> Cars = new ConcurrentHashMap<>();

    public synchronized static void addCamera(SocketHolder socketHolder) {
        Camera newCamera = new Camera(socketHolder);

        if (!Cameras.containsKey(newCamera.getRoad())) {
            Cameras.put(newCamera.getRoad(), new HashSet<>());
        }
        Limits.putIfAbsent(newCamera.getRoad(), newCamera.getLimit());

        if (newCamera.getLimit() != Limits.get(newCamera.getRoad())) {
            newCamera.sendError(Error.ErrorTypes.DIFFERENT_LIMIT_ERROR);
        }

        Cameras.get(newCamera.getRoad()).add(newCamera);

        new Thread(newCamera::run).start();
    }

    public synchronized static void addDispatcher(SocketHolder socketHolder) {
        Dispatcher newDispatcher = new Dispatcher(socketHolder);

        for (int road : newDispatcher.getRoads()) {
            if (!Dispatchers.containsKey(road)) {
                Dispatchers.put(road, new HashSet<>());
            }

            Dispatchers.get(road).add(newDispatcher);
        }
    }

    public static void disconnectAll() {
        for (Camera camera : Cameras.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            camera.disconnect();
        }

        Cameras.clear();

        for (Dispatcher dispatcher : Dispatchers.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            dispatcher.disconnect();
        }

        Dispatchers.clear();
    }
}

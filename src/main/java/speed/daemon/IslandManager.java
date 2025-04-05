package speed.daemon;

import speed.daemon.clientMessages.Plate;
import speed.daemon.codex.SocketHolder;
import speed.daemon.devices.Camera;
import speed.daemon.devices.Dispatcher;
import speed.daemon.serverMessages.Error;
import speed.daemon.serverMessages.Ticket;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class IslandManager {
    private static final Map<Integer, Set<Camera>> Cameras = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Dispatcher>> Dispatchers = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> Limits = new ConcurrentHashMap<>();
    private static final Map<String, Car> Cars = new ConcurrentHashMap<>();
    private static final Map<Integer, Queue<Ticket>> TicketQueues = new ConcurrentHashMap<>();

    public synchronized static void addCamera(SocketHolder socketHolder) {
        Camera newCamera = new Camera(socketHolder);

        if (!Cameras.containsKey(newCamera.getRoad())) {
            Cameras.put(newCamera.getRoad(), new HashSet<>());
            TicketQueues.put(newCamera.getRoad(), new ConcurrentLinkedQueue<>());
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

            if (TicketQueues.containsKey(road)) {
                Ticket ticket;
                while ((ticket = TicketQueues.get(road).poll()) != null) {
                    newDispatcher.sendTicket(ticket);
                }
            }
        }
    }

    public static void reportPlate(Plate plate, int road, int mile) {
        if (!Cars.containsKey(plate.getPlateNumber())) {
            Cars.put(plate.getPlateNumber(), new Car(plate.getPlateNumber()));
        }

        Car car = Cars.get(plate.getPlateNumber());

        car.lock();

        Ticket ticket = car.checkForTicket(plate.getTimestamp(), road, mile, Limits.get(road));

        if (ticket != null) {
            dispatchTicket(road, ticket);
        }

        car.addSighting(plate.getTimestamp(), road, mile);

        car.unlock();
    }

    private static void dispatchTicket(int road, Ticket ticket) {
        if (Dispatchers.containsKey(road) && !Dispatchers.get(road).isEmpty()) {
            Dispatchers.get(road).stream().findAny().orElseThrow().sendTicket(ticket);
        } else {
            TicketQueues.get(road).add(ticket);
        }
    }

    public static void remove(Camera camera) {
        Cameras.get(camera.getRoad()).remove(camera);
    }

    public static void remove(Dispatcher dispatcher) {
        for (int road : dispatcher.getRoads()) {
            Dispatchers.get(road).remove(dispatcher);
        }
    }

    public static void disconnectAll() {
        Cameras.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .forEach(Camera::disconnect);

        Dispatchers.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .forEach(Dispatcher::disconnect);

        Cameras.clear();
        Dispatchers.clear();
    }
}

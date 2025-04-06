package speed.daemon;

import lombok.RequiredArgsConstructor;
import speed.daemon.serverMessages.Ticket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class Car {
    private static final double LIMIT_TOLERANCE = 0.25;

    private final String PlateNumber;
    private final Map<Integer, Map<Long, Integer>> Sightings = new HashMap<>();
    private final Set<Long> DaysTicketed = new HashSet<>();
    private final Lock Lock = new ReentrantLock();

    public void lock() {
        Lock.lock();
    }

    public void unlock() {
        Lock.unlock();
    }

    public void addSighting(long timestamp, int road, int mile) {
        if (!Sightings.containsKey(road)) {
            Sightings.put(road, new HashMap<>());
        }

        Sightings.get(road).put(timestamp, mile);
    }

    public Ticket checkForTicket(long timestamp, int road, int mile, int limit) {
        if (!Sightings.containsKey(road)) {
            Sightings.put(road, new HashMap<>());
        }

        Set<Ticket> potentialTickets = new HashSet<>();

        Map<Long, Integer> roadSightings = Sightings.get(road);

        for (long pictureTimestamp : roadSightings.keySet()) {
            long deltaTimeSeconds = Math.abs(timestamp - pictureTimestamp);
            double deltaTimeHours = ((double) deltaTimeSeconds) / 3_600;

            int deltaDistance = Math.abs(mile - roadSightings.get(pictureTimestamp));

            double averageSpeed = ((double) deltaDistance) / deltaTimeHours;

            if (averageSpeed > limit + LIMIT_TOLERANCE) {
                potentialTickets.add(Ticket.builder()
                        .Plate(this.PlateNumber)
                        .Road(road)
                        .Mile1(timestamp < pictureTimestamp ? mile : roadSightings.get(pictureTimestamp))
                        .Timestamp1(Math.min(timestamp, pictureTimestamp))
                        .Mile2(timestamp > pictureTimestamp ? mile : roadSightings.get(pictureTimestamp))
                        .Timestamp2(Math.max(timestamp, pictureTimestamp))
                        .Speed((int) (averageSpeed * 100))
                        .build());
            }
        }

        Set<Ticket> ticketsToRemove = new HashSet<>();

        for (Ticket potentialTicket : potentialTickets) {
            long dayMin = potentialTicket.getTimestamp1() / 86400;
            long dayMax = potentialTicket.getTimestamp2() / 86400;

            for (long i = dayMin; i <= dayMax; i++) {
                if (DaysTicketed.contains(i)) {
                    ticketsToRemove.add(potentialTicket);
                    break;
                }
            }
        }

        potentialTickets.removeAll(ticketsToRemove);

        if (potentialTickets.isEmpty()) {
            return null;
        } else {
            Ticket ticket = potentialTickets.stream().findAny().orElseThrow();

            long dayMin = ticket.getTimestamp1() / 86400;
            long dayMax = ticket.getTimestamp2() / 86400;

            for (long i = dayMin; i <= dayMax; i++) {
                DaysTicketed.add(i);
            }

            return ticket;
        }
    }
}

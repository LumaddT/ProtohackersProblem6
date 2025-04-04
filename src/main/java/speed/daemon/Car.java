package speed.daemon;

import lombok.RequiredArgsConstructor;
import speed.daemon.serverMessages.Ticket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class Car {
    private static final double LIMIT_TOLERANCE = 0.5;

    private final String PlateNumber;
    private final Map<Integer, Map<Long, Integer>> Pictures = new ConcurrentHashMap<>();
    private final Set<Long> DaysTicketed = new HashSet<>();

    public Ticket addPicture(long timestamp, int road, int mile, int limit) {
        if (!Pictures.containsKey(road)) {
            Pictures.put(road, new ConcurrentHashMap<>());
        }

        Ticket ticket = checkForTicket(timestamp, road, mile, limit);

        Pictures.get(road).put(timestamp, mile);

        return ticket;
    }

    private Ticket checkForTicket(long timestamp, int road, int mile, int limit) {
        Set<Ticket> potentialTickets = new HashSet<>();

        Map<Long, Integer> roadPictures = Pictures.get(road);

        for (long pictureTimestamp : roadPictures.keySet()) {
            long deltaTimeSeconds = Math.abs(timestamp - pictureTimestamp);
            double deltaTimeHours = ((double) deltaTimeSeconds) / 3_600;

            int deltaDistance = Math.abs(mile - roadPictures.get(pictureTimestamp));

            double averageSpeed = ((double) deltaDistance) / deltaTimeHours;

            if (averageSpeed > limit) {
                potentialTickets.add(Ticket.builder()
                        .Plate(this.PlateNumber)
                        .Road(road)
                        .Mile1(timestamp < pictureTimestamp ? mile : roadPictures.get(pictureTimestamp))
                        .Timestamp1(Math.min(timestamp, pictureTimestamp))
                        .Mile2(timestamp > pictureTimestamp ? mile : roadPictures.get(pictureTimestamp))
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

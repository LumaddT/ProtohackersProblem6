package speed.daemon.devices;

import lombok.Getter;
import speed.daemon.IslandManager;
import speed.daemon.MessageTypes;
import speed.daemon.clientMessages.IAmDispatcher;
import speed.daemon.SocketHolder;
import speed.daemon.exceptions.SocketIsDeadException;
import speed.daemon.serverMessages.Ticket;

import java.util.Collections;
import java.util.Set;

@Getter
public class Dispatcher {
    private final SocketHolder SocketHolder;
    private final Set<Integer> Roads;

    public Dispatcher(SocketHolder socketHolder) {
        if (socketHolder.getInitialMessage().getMessageType() != MessageTypes.I_AM_DISPATCHER) {
            throw new RuntimeException("SocketHolder with initial message type %s sent to Dispatcher constructor. This should never happen."
                    .formatted(socketHolder.getInitialMessage().getMessageType().name()));
        }

        IAmDispatcher initialMessage = (IAmDispatcher) socketHolder.getInitialMessage();

        SocketHolder = socketHolder;
        Roads = Collections.unmodifiableSet(initialMessage.getRoads());
    }

    public void sendTicket(Ticket ticket) {
        try {
            SocketHolder.sendMessage(ticket);
        } catch (SocketIsDeadException e) {
            this.disconnect();
        }
    }

    public void disconnect() {
        SocketHolder.close();
        IslandManager.remove(this);
    }
}

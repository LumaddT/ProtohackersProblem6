package speed.daemon.devices;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.IslandManager;
import speed.daemon.MessageTypes;
import speed.daemon.clientMessages.IAmDispatcher;
import speed.daemon.codex.SocketHolder;
import speed.daemon.exceptions.ImpossibleEncodingException;
import speed.daemon.exceptions.SocketIsDeadException;
import speed.daemon.serverMessages.Ticket;

import java.util.Collections;
import java.util.Set;

@Getter
public class Dispatcher {
    private static final Logger logger = LogManager.getLogger();

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
        byte[] encodedTicket;
        try {
            encodedTicket = ticket.encode();
        } catch (ImpossibleEncodingException e) {
            logger.fatal("An unrecoverable error occurred while encoding an ticket message.");

            throw new RuntimeException(e);
        }

        try {
            SocketHolder.sendMessage(encodedTicket);
        } catch (SocketIsDeadException e) {
            this.disconnect();
        }
    }

    public void disconnect() {
        SocketHolder.close();
        IslandManager.remove(this);
    }
}

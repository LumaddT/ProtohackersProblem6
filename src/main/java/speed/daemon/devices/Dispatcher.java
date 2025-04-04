package speed.daemon.devices;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.codex.SocketHolder;
import speed.daemon.exceptions.ImpossibleEncodingException;
import speed.daemon.exceptions.SocketIsDeadException;
import speed.daemon.serverMessages.Ticket;

@RequiredArgsConstructor
public class Dispatcher {
    private static final Logger logger = LogManager.getLogger();

    private final SocketHolder SocketHolder;

    public void run() {
        // TODO
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

        // TODO: clean from objects holder
    }
}

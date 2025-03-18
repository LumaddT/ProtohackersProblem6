package speed.daemon.devices;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

@RequiredArgsConstructor
public class Dispatcher {
    private static final Logger logger = LogManager.getLogger();

    private final Socket Socket;

    public void disconnect() {
        try {
            Socket.close();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while closing a Dispatcher socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }
}

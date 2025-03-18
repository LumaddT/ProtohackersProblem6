package speed.daemon.devices;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

@RequiredArgsConstructor
public class Camera {
    private static final Logger logger = LogManager.getLogger();

    private final Socket Socket;
    private final int Mile;

    public void disconnect() {
        try {
            Socket.close();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while closing a Camera socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

}

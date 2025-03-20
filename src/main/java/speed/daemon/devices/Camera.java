package speed.daemon.devices;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.Island;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@RequiredArgsConstructor
public class Camera {
    private static final Logger logger = LogManager.getLogger();

    private final Socket Socket;
    private final InputStream InputStream;
    private final OutputStream OutputStream;
    private final int Mile;
    private final Island ParentIsland;

    public void disconnect() {
        try {
            Socket.close();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while closing a Camera socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

}

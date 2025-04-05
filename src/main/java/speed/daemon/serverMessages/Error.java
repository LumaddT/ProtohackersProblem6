package speed.daemon.serverMessages;

import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.MessageTypes;
import speed.daemon.MessageEncoder;
import speed.daemon.exceptions.ImpossibleEncodingException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ToString
public class Error {
    private static final Logger logger = LogManager.getLogger();

    public static final Map<ErrorTypes, byte[]> ERRORS;

    private final String Message;

    static {
        Map<ErrorTypes, byte[]> errors = new HashMap<>();

        for (ErrorTypes errorType : ErrorTypes.values()) {
            try {
                errors.put(errorType, new Error(errorType).encode());
            } catch (ImpossibleEncodingException e) {
                logger.fatal("An unrecoverable error occurred while encoding an error message.");
                throw new RuntimeException(e);
            }
        }

        ERRORS = Collections.unmodifiableMap(errors);
    }

    private Error(ErrorTypes errorType) {
        Message = errorType.ErrorMessage;
    }

    public byte[] encode() throws ImpossibleEncodingException {
        byte[] encoded = new byte[Message.length() + 2];
        encoded[0] = MessageTypes.ERROR.getFlag();

        byte[] encodedMessage = MessageEncoder.encodeString(Message);
        System.arraycopy(encodedMessage, 0, encoded, 1, encodedMessage.length);

        return encoded;
    }

    public enum ErrorTypes {
        UNEXPECTED_MESSAGE_TYPE("The client began a message with an unexpected message type."),
        EXPECTED_MORE_BYTES("The client sent an EOF before finishing a complete message."),
        DOUBLE_HEARTBEAT_ERROR("The client send a WantHeartbeat message, but heartbeat is already being send."),
        DIFFERENT_LIMIT_ERROR("The client Camera sent a limit for a road different from the one already recorded.");

        private final String ErrorMessage;

        ErrorTypes(String errorMessage) {
            ErrorMessage = errorMessage;
        }
    }
}

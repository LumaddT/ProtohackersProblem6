package speed.daemon.serverMessages;

import speed.daemon.MessageTypes;
import speed.daemon.exceptions.ImpossibleEncodingException;

public interface ServerMessage {
    MessageTypes getMessageType();

    byte[] encode() throws ImpossibleEncodingException;
}

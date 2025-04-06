package speed.daemon.serverMessages;

import speed.daemon.exceptions.ImpossibleEncodingException;

public interface ServerMessage {
    byte[] encode() throws ImpossibleEncodingException;
}

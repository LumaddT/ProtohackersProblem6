package speed.daemon.serverMessages;

import lombok.Getter;
import lombok.ToString;
import speed.daemon.MessageTypes;

@Getter
@ToString
public class Heartbeat implements ServerMessage {
    private final MessageTypes MessageType = MessageTypes.HEARTBEAT;

    public static final Heartbeat INSTANCE = new Heartbeat();

    @Override
    public byte[] encode() {
        byte[] encoded = new byte[1];
        encoded[0] = MessageTypes.HEARTBEAT.getFlag();
        return encoded;
    }
}

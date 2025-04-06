package speed.daemon.serverMessages;

import lombok.ToString;
import speed.daemon.MessageTypes;

@ToString
public class Heartbeat implements ServerMessage {
    public static final Heartbeat INSTANCE = new Heartbeat();

    @Override
    public byte[] encode() {
        byte[] encoded = new byte[1];
        encoded[0] = MessageTypes.HEARTBEAT.getFlag();
        return encoded;
    }
}

package speed.daemon.serverMessages;

import lombok.ToString;
import speed.daemon.MessageTypes;

@ToString
public class Heartbeat {
    public static final byte[] ENCODED = new Heartbeat().encode();

    public byte[] encode() {
        byte[] encoded = new byte[1];
        encoded[0] = MessageTypes.HEARTBEAT.getFlag();
        return encoded;
    }
}

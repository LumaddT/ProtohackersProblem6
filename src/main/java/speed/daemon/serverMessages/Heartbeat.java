package speed.daemon.serverMessages;

public class Heartbeat implements ServerMessage {
    @Override
    public int[] encode() {
        return new int[0];
    }
}

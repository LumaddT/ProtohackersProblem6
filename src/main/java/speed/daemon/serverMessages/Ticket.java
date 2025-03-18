package speed.daemon.serverMessages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Ticket implements ServerMessage {
    private final String Plate;
    private final int Road;
    private final int Mile1;
    private final long Timestamp1;
    private final int Mile2;
    private final long Timestamp2;
    private final int Speed;

    @Override
    public int[] encode() {
        return new int[0];
    }
}

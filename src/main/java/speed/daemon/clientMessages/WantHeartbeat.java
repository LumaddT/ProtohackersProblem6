package speed.daemon.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import speed.daemon.MessageTypes;

@Getter
@RequiredArgsConstructor
@ToString
public class WantHeartbeat implements ClientMessage {
    private final MessageTypes MessageType = MessageTypes.WANT_HEARTBEAT;

    private final long Interval;
}

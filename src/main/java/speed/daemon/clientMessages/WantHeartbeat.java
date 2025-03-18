package speed.daemon.clientMessages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WantHeartbeat implements ClientMessage {
    private final long Interval;
}

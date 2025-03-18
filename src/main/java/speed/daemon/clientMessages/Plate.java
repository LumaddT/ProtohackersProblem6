package speed.daemon.clientMessages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Plate implements ClientMessage {
    private final String Plate;
    private final long Timestamp; // Necessary since there is no unsigned int
}

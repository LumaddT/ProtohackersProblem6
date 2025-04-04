package speed.daemon.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import speed.daemon.MessageTypes;

@Getter
@RequiredArgsConstructor
@ToString
public class Plate implements ClientMessage {
    private final MessageTypes MessageType = MessageTypes.PLATE;

    private final String PlateNumber;
    private final long Timestamp; // Necessary since there is no unsigned int
}

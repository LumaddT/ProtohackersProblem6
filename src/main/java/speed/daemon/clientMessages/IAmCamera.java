package speed.daemon.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import speed.daemon.MessageTypes;

@Getter
@RequiredArgsConstructor
@ToString
public class IAmCamera implements ClientMessage {
    private final MessageTypes MessageType = MessageTypes.I_AM_CAMERA;

    private final int Road;
    private final int Mile;
    private final int Limit;
}

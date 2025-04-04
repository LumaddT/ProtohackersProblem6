package speed.daemon.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import speed.daemon.MessageTypes;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@ToString
public class IAmDispatcher implements ClientMessage {
    private final MessageTypes MessageType = MessageTypes.I_AM_DISPATCHER;

    private final Set<Integer> Roads;
}

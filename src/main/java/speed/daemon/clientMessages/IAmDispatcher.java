package speed.daemon.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class IAmDispatcher implements ClientMessage {
    private final Set<Integer> Roads;
}

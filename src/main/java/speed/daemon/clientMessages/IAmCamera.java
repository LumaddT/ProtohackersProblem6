package speed.daemon.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IAmCamera implements ClientMessage {
    private final int Road;
    private final int Mile;
    private final int Limit;
}

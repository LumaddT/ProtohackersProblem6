package speed.daemon;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

enum MessageTypes {
    ERROR(0x10),
    PLATE(0x20),
    TICKET(0x21),
    WANT_HEARTBEAT(0x40),
    HEARTBEAT(0x41),
    I_AM_CAMERA(0x80),
    I_AM_DISPATCHER(0x81);

    @Getter
    private final int Flag;
    private static final Map<Integer, MessageTypes> FLAGS_TO_TYPES;

    public static final Set<MessageTypes> CLIENT_MESSAGES = Set.of(PLATE, WANT_HEARTBEAT, I_AM_CAMERA, I_AM_DISPATCHER);
    public static final Set<MessageTypes> SERVER_MESSAGES = Set.of(ERROR, TICKET, HEARTBEAT);

    static {
        FLAGS_TO_TYPES = EnumSet.allOf(MessageTypes.class).stream()
                .collect(Collectors.toMap(MessageTypes::getFlag, n -> n));
    }

    MessageTypes(int flag) {
        Flag = flag;
    }

    public static MessageTypes getType(int flag) {
        return FLAGS_TO_TYPES.get(flag);
    }
}

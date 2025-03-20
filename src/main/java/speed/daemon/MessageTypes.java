package speed.daemon;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum MessageTypes {
    ERROR((byte) 0x10),
    PLATE((byte) 0x20),
    TICKET((byte) 0x21),
    WANT_HEARTBEAT((byte) 0x40),
    HEARTBEAT((byte) 0x41),
    I_AM_CAMERA((byte) 0x80),
    I_AM_DISPATCHER((byte) 0x81);

    private final byte Flag;
    private static final Map<Byte, MessageTypes> FLAGS_TO_TYPES;

    public static final Set<MessageTypes> CLIENT_MESSAGES = Set.of(PLATE, WANT_HEARTBEAT, I_AM_CAMERA, I_AM_DISPATCHER);
    public static final Set<MessageTypes> SERVER_MESSAGES = Set.of(ERROR, TICKET, HEARTBEAT);

    static {
        FLAGS_TO_TYPES = EnumSet.allOf(MessageTypes.class).stream()
                .collect(Collectors.toMap(MessageTypes::getFlag, n -> n));
    }

    MessageTypes(byte flag) {
        Flag = flag;
    }

    public static MessageTypes getType(byte flag) {
        return FLAGS_TO_TYPES.get(flag);
    }
}

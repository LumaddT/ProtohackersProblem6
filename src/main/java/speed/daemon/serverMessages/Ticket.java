package speed.daemon.serverMessages;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import speed.daemon.MessageTypes;
import speed.daemon.MessageEncoder;
import speed.daemon.exceptions.ImpossibleEncodingException;

@Builder
@ToString
public class Ticket implements ServerMessage {
    @Getter
    private final MessageTypes MessageType = MessageTypes.TICKET;

    private final String Plate;
    private final int Road;
    private final int Mile1;
    @Getter
    private final long Timestamp1;
    private final int Mile2;
    @Getter
    private final long Timestamp2;
    private final int Speed;

    @Override
    public byte[] encode() throws ImpossibleEncodingException {
        byte[] encoded = new byte[Plate.length() + 18];
        encoded[0] = MessageTypes.TICKET.getFlag();

        int destinationIndex = 1;

        byte[] encodedPlate = MessageEncoder.encodeString(Plate);
        System.arraycopy(encodedPlate, 0, encoded, destinationIndex, encodedPlate.length);
        destinationIndex += encodedPlate.length;

        byte[] encodedRoad = MessageEncoder.encodeU16(Road);
        System.arraycopy(encodedRoad, 0, encoded, destinationIndex, encodedRoad.length);
        destinationIndex += encodedRoad.length;

        byte[] encodedMile1 = MessageEncoder.encodeU16(Mile1);
        System.arraycopy(encodedMile1, 0, encoded, destinationIndex, encodedMile1.length);
        destinationIndex += encodedMile1.length;

        byte[] encodedTimestamp1 = MessageEncoder.encodeU32(Timestamp1);
        System.arraycopy(encodedTimestamp1, 0, encoded, destinationIndex, encodedTimestamp1.length);
        destinationIndex += encodedTimestamp1.length;

        byte[] encodedMile2 = MessageEncoder.encodeU16(Mile2);
        System.arraycopy(encodedMile2, 0, encoded, destinationIndex, encodedMile2.length);
        destinationIndex += encodedMile2.length;

        byte[] encodedTimestamp2 = MessageEncoder.encodeU32(Timestamp2);
        System.arraycopy(encodedTimestamp2, 0, encoded, destinationIndex, encodedTimestamp2.length);
        destinationIndex += encodedTimestamp2.length;

        byte[] encodedSpeed = MessageEncoder.encodeU16(Speed);
        System.arraycopy(encodedSpeed, 0, encoded, destinationIndex, encodedSpeed.length);

        return encoded;
    }
}

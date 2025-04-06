package speed.daemon.codex;

import speed.daemon.MessageTypes;
import speed.daemon.clientMessages.*;
import speed.daemon.exceptions.ExpectedMoreBytesException;
import speed.daemon.exceptions.UnexpectedMessageTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageReceiver {
    public static ClientMessage receiveClientMessage(InputStream inputStream, Collection<MessageTypes> expectedMessageTypes) throws UnexpectedMessageTypeException, ExpectedMoreBytesException, IOException {
        int firstByte = receiveByte(inputStream);

        MessageTypes messageType = MessageTypes.getType((byte) firstByte);

        if (messageType == null || !expectedMessageTypes.contains(messageType)) {
            String exceptionMessage = "Expected message with type any of {" +
                    expectedMessageTypes.stream()
                            .map(MessageTypes::name)
                            .collect(Collectors.joining(", ")) +
                    "} but found \"" +
                    (messageType != null ? messageType.name() : "0x%02X".formatted(firstByte)) +
                    "\" instead.";

            throw new UnexpectedMessageTypeException(exceptionMessage);
        }

        return switch (messageType) {
            case I_AM_CAMERA -> receiveIAmCamera(inputStream);
            case I_AM_DISPATCHER -> receiveIAmDispatcher(inputStream);
            case PLATE -> receivePlate(inputStream);
            case WANT_HEARTBEAT -> receiveWantHeartbeat(inputStream);
            default ->
                    throw new UnexpectedMessageTypeException("Expected a client message type but found \"%s\" instead.".formatted(messageType.name()));
        };
    }

    private static Plate receivePlate(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        String plate = receiveString(inputStream);
        long timestamp = receiveU32(inputStream);

        return new Plate(plate, timestamp);
    }

    private static WantHeartbeat receiveWantHeartbeat(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        long interval = receiveU32(inputStream);

        return new WantHeartbeat(interval);
    }

    private static IAmCamera receiveIAmCamera(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        int road = receiveU16(inputStream);
        int mile = receiveU16(inputStream);
        int limit = receiveU16(inputStream);

        return new IAmCamera(road, mile, limit);
    }

    private static IAmDispatcher receiveIAmDispatcher(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        int numRoads = receiveU8(inputStream);
        Set<Integer> roads = new HashSet<>();

        for (int i = 0; i < numRoads; i++) {
            roads.add(receiveU16(inputStream));
        }

        return new IAmDispatcher(Collections.unmodifiableSet(roads));
    }

    private static String receiveString(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        StringBuilder returnValue = new StringBuilder();

        int length = receiveByte(inputStream);

        for (int i = 0; i < length; i++) {
            int inputByte = receiveByte(inputStream);

            returnValue.append((char) inputByte);
        }

        return returnValue.toString();
    }

    private static int receiveU8(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        return receiveByte(inputStream);
    }

    private static int receiveU16(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        int returnValue = 0;

        for (int i = 0; i < 2; i++) {
            int inputByte = receiveByte(inputStream);

            returnValue += inputByte << (8 * (1 - i));
        }

        return returnValue;
    }

    private static long receiveU32(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        long returnValue = 0;

        for (int i = 0; i < 4; i++) {
            long inputByte = receiveByte(inputStream);

            returnValue += inputByte << (8 * (3 - i));
        }

        return returnValue;
    }

    private static int receiveByte(InputStream inputStream) throws IOException, ExpectedMoreBytesException {
        int inputByte = inputStream.read();

        if (inputByte == -1) {
            throw new ExpectedMoreBytesException("Reached EOF while expecting new bytes.");
        }

        return inputByte;
    }
}

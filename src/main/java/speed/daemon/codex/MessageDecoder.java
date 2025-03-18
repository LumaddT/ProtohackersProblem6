package speed.daemon.codex;

import speed.daemon.exceptions.ExpectedMoreBytesException;
import speed.daemon.clientMessages.IAmCamera;
import speed.daemon.clientMessages.IAmDispatcher;
import speed.daemon.clientMessages.Plate;
import speed.daemon.clientMessages.WantHeartbeat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MessageDecoder {
    public static Plate parsePlate(InputStream inputStream) throws IOException {
        String plate = parseString(inputStream);
        long timestamp = parseU32(inputStream);

        return new Plate(plate, timestamp);
    }

    public static WantHeartbeat parseWantHeartbeat(InputStream inputStream) throws IOException {
        long interval = parseU32(inputStream);

        return new WantHeartbeat(interval);
    }

    public static IAmCamera parseIAmCamera(InputStream inputStream) throws IOException {
        int road = parseU16(inputStream);
        int mile = parseU16(inputStream);
        int limit = parseU16(inputStream);

        return new IAmCamera(road, mile, limit);
    }

    public static IAmDispatcher parseIAmDispatcher(InputStream inputStream) throws IOException {
        int numRoads = parseU8(inputStream);
        Set<Integer> roads = new HashSet<>();

        for (int i = 0; i < numRoads; i++) {
            roads.add(parseU16(inputStream));
        }

        return new IAmDispatcher(Collections.unmodifiableSet(roads));
    }

    private static String parseString(InputStream inputStream) throws IOException {
        StringBuilder returnValue = new StringBuilder();

        int length = readByte(inputStream);

        for (int i = 0; i < length; i++) {
            int inputByte = readByte(inputStream);

            returnValue.append((char) inputByte);
        }

        return returnValue.toString();
    }

    private static int parseU8(InputStream inputStream) throws IOException {
        return readByte(inputStream);
    }

    private static int parseU16(InputStream inputStream) throws IOException {
        int returnValue = 0;

        for (int i = 0; i < 2; i++) {
            int inputByte = readByte(inputStream);

            returnValue += inputByte << (8 * (1 - i));
        }

        return returnValue;
    }

    private static long parseU32(InputStream inputStream) throws IOException {
        long returnValue = 0;

        for (int i = 0; i < 4; i++) {
            long inputByte = readByte(inputStream);

            returnValue += inputByte << (8 * (3 - i));
        }

        return returnValue;
    }

    private static int readByte(InputStream inputStream) throws IOException {
        int inputByte = inputStream.read();

        if (inputByte == -1) {
            throw new ExpectedMoreBytesException("Reached EOF while expecting new bytes.");
        }

        return inputByte;
    }
}

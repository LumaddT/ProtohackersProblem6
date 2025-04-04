package speed.daemon;

import speed.daemon.exceptions.ImpossibleEncodingException;

import java.nio.charset.StandardCharsets;

public class MessageEncoder {
    private static final int U8_MAX = 0b11111111;
    private static final int U16_MAX = 0b11111111_11111111;
    private static final long U32_MAX = 0b11111111_11111111_11111111_11111111L;

    public static byte[] encodeString(String string) throws ImpossibleEncodingException {
        if (string.length() > 255) {
            throw new ImpossibleEncodingException("Attempted to encode a String of length %s but maximum length is 255.".formatted(string.length()));
        }

        byte[] encoded = new byte[string.length() + 1];

        encoded[0] = encodeU8(string.length());

        int i = 1;
        for (byte b : string.getBytes(StandardCharsets.US_ASCII)) {
            encoded[i] = b;
            i++;
        }

        return encoded;
    }

    public static byte encodeU8(int num) throws ImpossibleEncodingException {
        if (num > U8_MAX || num < 0) {
            throw new ImpossibleEncodingException("Attempted to encode int %d as u8 but max is %d.".formatted(num, U8_MAX));
        }

        return (byte) num;
    }

    public static byte[] encodeU16(int num) throws ImpossibleEncodingException {
        if (num > U16_MAX || num < 0) {
            throw new ImpossibleEncodingException("Attempted to encode int %d as u8 but max is %d.".formatted(num, U16_MAX));
        }

        byte[] encoded = new byte[2];

        encoded[0] = (byte) (num >>> 8);
        encoded[1] = (byte) num;

        return encoded;
    }

    public static byte[] encodeU32(long num) throws ImpossibleEncodingException {
        if (num > U32_MAX || num < 0) {
            throw new ImpossibleEncodingException("Attempted to encode long %d as u8 but max is %d.".formatted(num, U32_MAX));
        }

        byte[] encoded = new byte[4];

        encoded[0] = (byte) (num >>> 24);
        encoded[1] = (byte) (num >>> 16);
        encoded[2] = (byte) (num >>> 8);
        encoded[3] = (byte) num;

        return encoded;
    }
}

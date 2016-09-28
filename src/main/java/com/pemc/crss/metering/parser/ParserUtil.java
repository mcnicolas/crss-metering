package com.pemc.crss.metering.parser;

import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;

public class ParserUtil {

    private ParserUtil() {
    }

    @Deprecated
    // TODO: Check if it is better to return an int
    public static String parseInt(int start, int end, byte[] buffer) {
        char c = (char) ((buffer[start] & 0x00FF) | Character.reverseBytes((char) buffer[end]));
        return Integer.toString((int) c).trim();
    }

    @Deprecated
    public static String parseText(int start, int end, byte[] buffer) {
        String retVal = "";

        while (start <= end) {
            retVal = retVal + (char) buffer[start];
            start++;
        }

        return retVal.trim();
    }

    public static String parseText(ByteBuffer buffer, int size) {
        byte[] data = new byte[size];
        buffer.get(data);

        return new String(data).trim();
    }

    public static String parseText(ByteBuffer buffer, int offset, int size) {
        buffer.get(new byte[offset]);

        byte[] data = new byte[size];
        buffer.get(data);

        return new String(data).trim();
    }

    public static String convertToBinaryString(char a) {
        return StringUtils.leftPad(Integer.toBinaryString((int) a), 16, "0");
    }

}

package com.pemc.crss.metering.parser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserUtil {

    private ParserUtil() {
    }

    // TODO: Check if it is better to return an int
    public static String parseInt(int start, int end, byte[] buffer) {
        char c = (char) ((buffer[start] & 0x00FF) | Character.reverseBytes((char) buffer[end]));
        return Integer.toString((int) c).trim();
    }

    public static String parseText(int start, int end, byte[] buffer) {
        String retVal = "";

        while (start <= end) {
            retVal = retVal + (char) buffer[start];
            start++;
        }

        return retVal.trim();
    }

    public static String convertToBinaryString(char a) {
        String binaryString = Integer.toBinaryString((int) a);
        binaryString = StringUtils.leftPad(binaryString, 16, "0");

        return binaryString;
    }

}

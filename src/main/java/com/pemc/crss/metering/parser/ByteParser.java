package com.pemc.crss.metering.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteParser {

    private static final Logger LOG = LoggerFactory.getLogger(ByteParser.class);

    private ByteParser() {
    }

    public static String parse(int start, int end, char type, byte[] b) {

        String transformedData = "";
        char c;

        try {
            if (type == 'i') {
                /**
                 * byte to integer of 2 bytes, thus used char data type
                 * MDEF follows little-endian format. get Least Signicant Byte (LSB)
                 */
//				c = (char) (((char) b[start] & (char) 0x00FF) | (char) ((char) b[end] << 8));
//				transformedData = new Integer((int) c).toString();
//				System.out.println("version 1 "+transformedData);
                c = (char) ((b[start] & 0x00FF) | Character.reverseBytes((char) b[end]));
                transformedData = new Integer((int) c).toString();
//				System.out.println("version 2 "+transformedData);
            } else if (type == 'c') {
                while (start <= end) {
                    transformedData = transformedData + (char) b[start];
                    start++;
                }
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return transformedData.trim();
    }

}

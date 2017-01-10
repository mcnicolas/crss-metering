package com.pemc.crss.meter.upload.util;

public final class ErrorParserUtil {

    private ErrorParserUtil() {
    }

    public static String parseErrorMessage(String errorMessage) {
        String retVal;

        int index = errorMessage.indexOf(":");
        if (index != -1) {
            retVal = errorMessage.substring(index + 1).trim();
        } else {
            retVal = errorMessage;
        }

        return retVal;
    }

}

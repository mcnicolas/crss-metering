package com.pemc.crss.metering.validator.util;

public class BcqErrorMessageFormatter {

    private final static String LINE_ERROR_TEMPLATE = "LINE %d ERROR: ";
    private final static String FOUND_TEXT_TEMPLATE = "\nFOUND: %s";
    private final static String VALID_TEXT_TEMPLATE = "\nVALID: %s";

    public static String formatMessage(int lineNo, String errorMessage) {
        if (lineNo > 0) {
            return String.format(LINE_ERROR_TEMPLATE, lineNo) + errorMessage;
        }

        return String.format("ERROR: %s", errorMessage);
    }

    public static String formatMessage(int lineNo, String errorMessage, Object found) {
        String message = formatMessage(lineNo, errorMessage);

        return message.concat(String.format(FOUND_TEXT_TEMPLATE, found.toString()));
    }

    public static String formatMessage(int lineNo, String errorMessage, Object found, Object valid) {
        String message = formatMessage(lineNo, errorMessage, found);

        return message.concat(String.format(VALID_TEXT_TEMPLATE, valid.toString()));
    }
}

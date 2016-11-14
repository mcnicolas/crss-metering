package com.pemc.crss.metering.validator.util;

import com.pemc.crss.metering.constants.BcqValidationMessage;

public class BcqErrorMessageFormatter {

    private final static String LINE_ERROR_TEMPLATE = "LINE %d ERROR: ";
    private final static String FOUND_TEXT_TEMPLATE = "\nFOUND: %s";
    private final static String VALID_TEXT_TEMPLATE = "\nVALID: %s";

    public static String formatMessage(int lineNo, BcqValidationMessage validationMessage) {
        if (lineNo > 0) {
            return String.format(LINE_ERROR_TEMPLATE, lineNo) +
                    validationMessage.getMessage();
        }

        return String.format("ERROR: %s", validationMessage.getMessage());
    }

    public static String formatMessage(int lineNo, BcqValidationMessage validationMessage, Object found) {
        String message = formatMessage(lineNo, validationMessage);

        return message.concat(String.format(FOUND_TEXT_TEMPLATE, found.toString()));
    }

    public static String formatMessage(int lineNo, BcqValidationMessage validationMessage, Object found, Object valid) {
        String message = formatMessage(lineNo, validationMessage, found);

        return message.concat(String.format(VALID_TEXT_TEMPLATE, valid.toString()));
    }
}

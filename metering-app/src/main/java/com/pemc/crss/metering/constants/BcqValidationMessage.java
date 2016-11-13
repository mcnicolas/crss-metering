package com.pemc.crss.metering.constants;

public enum BcqValidationMessage {
    INVALID_NO_OF_COLUMNS("Invalid number of columns."),
    INVALID_INTERVAL("Invalid interval."),
    MISSING_SELLING_MTN("Missing selling MTN."),
    MISSING_BUYING_PARTICIPANT("Missing buying participant."),
    MISSING_REFERENCE_MTN("Missing reference MTN."),
    MISSING_END_TIME("Missing end time."),
    INVALID_END_TIME_FORMAT("Invalid end time format."),
    INVALID_END_TIME_FRAME("Invalid end time."),
    MISSING_BCQ("Missing bcq."),
    INVALID_BCQ("Invalid bcq."),
    DUPLICATE("Duplicate record.");

    private final String message;

    BcqValidationMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

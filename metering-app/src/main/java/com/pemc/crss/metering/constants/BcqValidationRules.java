package com.pemc.crss.metering.constants;

public enum BcqValidationRules {

    EMPTY("No data found."),
    INCORRECT_DECLARED_INTERVAL("Incorrect declared interval. Interval %s is not valid."),
    INCORRECT_COLUMN_HEADER_COUNT("Incorrect column header count. Number of columns should only be 5."),
    INCORRECT_TIME_INTERVALS("Incorrect time interval. Value %s is not valid for interval %s"),
    DUPLICATE_DATE("Duplicate date. Duplicate entry for %s found under Selling MTN %s and Buyer %s."),
    INCOMPLETE_ENTRIES("Incomplete BCQ entries for date of %s. Selling MTN %s and Buyer %s should have %s entries."),
    INCORRECT_DATA_TYPE("Incorrect data type. %s should be %s."),
    INCORRECT_FORMAT("Incorrect data format. %s should follow the format %s."),
    CLOSED_TRADING_DATE("Trading date is closed. Submission of BCQ is closed for %s."),
    MISSING_SELLING_MTN("Missing selling MTN."),
    MISSING_BUYING_PARTICIPANT("Missing buying participant."),
    MISSING_REFERENCE_MTN("Missing reference MTN."),
    MISSING_DATE("Missing date."),
    MISSING_BCQ("Missing BCQ.");

    private final String errorMessage;

    BcqValidationRules(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

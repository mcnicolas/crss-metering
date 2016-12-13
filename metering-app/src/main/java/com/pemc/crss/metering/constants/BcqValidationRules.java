package com.pemc.crss.metering.constants;

public enum BcqValidationRules {

    EMPTY("No data found."),
    EMPTY_LINE("Empty line is not allowed."),
    INCORRECT_DECLARED_INTERVAL("Incorrect declared interval. Interval %s is not valid."),
    INCORRECT_COLUMN_HEADER_COUNT("Incorrect column header count. Number of columns should only be 5."),
    INCORRECT_TIME_INTERVALS("Incorrect time interval. Value %s is not valid for interval %s"),
    DUPLICATE_DATE("Duplicate date. Duplicate entry for %s found under Selling MTN %s and Buyer %s."),
    INCOMPLETE_ENTRIES("Incomplete BCQ entries for date of %s. Selling MTN %s and Buyer %s should have %s entries."),
    INCORRECT_DATA_TYPE("Incorrect data type. %s should be %s."),
    INCORRECT_FORMAT("Incorrect data format. %s should follow the format %s."),
    CLOSED_TRADING_DATE("Trading date is closed. Submission of BCQ is closed for %s."),
    INVALID_TRADING_DATE("Trading date is invalid. Only one trading date can be accepted per file."),
    NEGATIVE_BCQ("BCQ is Negative. Declared BCQs should not be negative."),
    MISSING_SELLING_MTN("Missing selling MTN."),
    MISSING_BILLING_ID("Missing billing ID."),
    MISSING_REFERENCE_MTN("Missing reference MTN."),
    MISSING_DATE("Missing date."),
    MISSING_BCQ("Missing BCQ."),
    INCOMPLETE_REDECLARATION_ENTRIES("Incomplete BCQ entries. " +
            "Redeclaration for date %s should also have entries of the ff. Selling MTN and Buyer pair(s):\n %s");

    private final String errorMessage;

    BcqValidationRules(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

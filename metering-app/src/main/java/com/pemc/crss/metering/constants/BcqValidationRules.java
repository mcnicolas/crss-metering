package com.pemc.crss.metering.constants;

public enum BcqValidationRules {

    EMPTY("No data found."),
    EMPTY_LINE("Empty line is not allowed."),
    INCORRECT_DECLARED_INTERVAL("Incorrect declared interval. Interval <b>%s</b> is not valid."),
    INCORRECT_COLUMN_HEADER_COUNT("Incorrect column header count. Number of columns should only be <b>5</b>."),
    INCORRECT_TIME_INTERVALS("Incorrect time interval. Value <b>%s</b> is not valid for interval <b>%s</b>"),
    DUPLICATE_DATE("Duplicate date. Duplicate entry for <b>%s</b> found under Selling MTN <b>%s</b> and Buyer <b>%s</b>."),
    INCOMPLETE_ENTRIES("Incomplete BCQ entries for date of <b>%s</b>. " +
            "Selling MTN <b>%s</b> and Buyer <b>%s</b> should have <b>%s</b> entries."),
    INCORRECT_DATA_TYPE("Incorrect data type. %s should be <b>%s</b>."),
    INCORRECT_FORMAT("Incorrect data format. <b>%s</b> should follow the format <b>%s</b>."),
    CLOSED_TRADING_DATE("Trading date is closed. Submission of BCQ is closed for <b>%s</b>."),
    INVALID_TRADING_DATE("Trading date is invalid. Only one trading date can be accepted per file."),
    NEGATIVE_BCQ("BCQ is Negative. Declared BCQs should not be negative."),
    MISSING_SELLING_MTN("Missing selling MTN."),
    MISSING_BILLING_ID("Missing billing ID."),
    MISSING_REFERENCE_MTN("Missing reference MTN."),
    MISSING_DATE("Missing date."),
    MISSING_BCQ("Missing BCQ."),
    INCOMPLETE_REDECLARATION_ENTRIES("Incomplete BCQ entries. " +
            "Redeclaration for date <b>%s</b> should also have entries of the ff. " +
            "Selling MTN and Buyer pair(s):\n <b>%s</b>");

    private final String errorMessage;

    BcqValidationRules(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

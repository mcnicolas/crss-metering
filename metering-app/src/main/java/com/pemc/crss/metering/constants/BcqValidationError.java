package com.pemc.crss.metering.constants;

import java.util.List;

import static java.util.Arrays.asList;

public enum BcqValidationError {

    INVALID_CSV_FILE("Invalid CSV file."),
    EMPTY("No data found."),
    EMPTY_LINE("Empty line is not allowed."),
    INCORRECT_DECLARED_INTERVAL("Incorrect declared interval. Interval <b>%s</b> is not valid."),
    INCORRECT_COLUMN_HEADER_COUNT("Incorrect column header count. Number of columns should be <b>5</b>."),
    INCORRECT_TIME_INTERVALS("Incorrect time interval. Value <b>%s</b> is not valid for interval <b>%s</b>"),
    DUPLICATE_DATE("Duplicate date. Duplicate entry for <b>%s</b> found under "
            + "Selling MTN <b>%s</b> and Billing ID <b>%s</b>."),
    INCOMPLETE_ENTRIES("Incomplete BCQ entries for date of <b>%s</b>. "
            + "Selling MTN <b>%s</b> and Billing ID <b>%s</b> should have <b>%s</b> entries."),
    INCORRECT_DATA_TYPE("Incorrect data type. BCQ should be in <b>number</b> format."),
    INCORRECT_DATE_FORMAT("Incorrect date format. Date should follow the format <b>yyyy-MM-dd HH:mm</b>."),
    CLOSED_TRADING_DATE("Trading date is closed. Submission of BCQ is closed for <b>%s</b>."),
    INVALID_TRADING_DATE("Trading date is invalid. Only one trading date can be accepted per file."),
    DIFFERENT_TRADING_DATE("Trading date is invalid. Trading date must be the same when overriding."),
    NEGATIVE_BCQ("BCQ is Negative. Declared BCQs should not be negative."),
    INVALID_BCQ_LENGTH("BCQ length is invalid. BCQ can only have a length of <b>19 for integer part</b>, "
            + "and <b>9 for fractional part</b>."),
    MISSING_INTERVAL("Missing interval."),
    MISSING_SELLING_MTN("Missing selling MTN."),
    MISSING_BILLING_ID("Missing billing ID."),
    MISSING_REFERENCE_MTN("Missing reference MTN."),
    MISSING_DATE("Missing date."),
    MISSING_BCQ("Missing BCQ."),
    INCOMPLETE_RESUBMISSION_ENTRIES("Incomplete BCQ entries. " +
            "Resubmission for date <b>%s</b> should also have entries of the ff. "
            + "Selling MTN and Billing ID pair(s):<br />%s"),
    INCOMPLETE_OVERRIDE_ENTRIES("Incomplete BCQ entries. " +
            "Override for date <b>%s</b> should also have entries of the ff. "
            + "Selling MTN and Billing ID pair(s):<br />%s"),
    SELLING_MTN_UNREGISTERED("Unregistered selling MTN. Selling MTN <b>%s</b> does not exist."),
    SELLING_MTN_NOT_OWNED("Unregistered selling MTN. "
            + "Selling MTN <b>%s</b> is not registered under Seller <b>%s (%s)</b>."),
    BILLING_ID_NOT_EXIST("Billing ID: <b>%s</b> does not exist."),
    NO_ACTIVE_CONTRACT("Active enrollment does not exist. "
            + "No active contract between Seller <b>%s (%s)</b> and Buyer <b>%s (%s)</b>."),
    REFERENCE_MTN_UNREGISTERED("Unregistered reference MTN. Reference MTN <b>%s</b> does not exist."),
    REFERENCE_MTN_NOT_IN_CONTRACT("Unregistered reference MTN. "
            + "Reference MTN <b>%s</b> is not registered under contract of "
            + "Seller <b>%s (%s)</b> and Buyer <b>%s (%s)</b>."),
    NO_SPECIAL_EVENT_FOUND(""),
    PARTICIPANTS_NOT_PRESENT_IN_SPECIAL_EVENT("Following participant(s) were not included in the special event for "
            + "the trading date of %s.<br />%s"),
    OVERRIDDEN_ENTRIES("Following entry(ies) have already been overridden for the trading date of %s.<br />%s."),
    DEADLINE_DATE_PASSED("Deadline date of special event for trading date <b>%s</b> and participants <b>%s</b> has "
            + "passed.");

    private final String errorMessage;

    BcqValidationError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static List<BcqValidationError> CRSS_SIDE_ERRORS = asList(SELLING_MTN_UNREGISTERED, SELLING_MTN_NOT_OWNED,
            BILLING_ID_NOT_EXIST, NO_ACTIVE_CONTRACT, REFERENCE_MTN_UNREGISTERED, REFERENCE_MTN_NOT_IN_CONTRACT);

}

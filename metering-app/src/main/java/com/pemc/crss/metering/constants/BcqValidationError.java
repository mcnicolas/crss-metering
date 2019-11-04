package com.pemc.crss.metering.constants;

public enum BcqValidationError {

    INVALID_CSV_FILE("Invalid CSV file."),
    EMPTY("No data found."),
    EMPTY_LINE("Empty line is not allowed."),
    INCORRECT_DECLARED_INTERVAL("Incorrect declared interval. Interval <b>%s</b> is not valid."),
    INCORRECT_COLUMN_HEADER_COUNT("Incorrect column header count. Number of columns should be <b>5</b>."),
    INCORRECT_TIME_INTERVALS("Incorrect time interval. Value <b>%s</b> is not valid for interval <b>%s</b>"),
    DUPLICATE_DATE("Duplicate date. Duplicate entry for <b>%s</b> found under "
            + "Seller MTN <b>%s</b> and Billing ID <b>%s</b>."),
    INCOMPLETE_ENTRIES("Incomplete BCQ entries for date of <b>%s</b>. "
            + "Seller MTN <b>%s</b> and Billing ID <b>%s</b> should have <b>%s</b> entries."),
    INCORRECT_DATA_TYPE("Incorrect data type. BCQ should be in <b>number</b> format."),
    INCORRECT_DATE_FORMAT("Incorrect date format. Date should follow the format <b>yyyy-MM-dd HH:mm</b>."),
    CLOSED_TRADING_DATE("Trading date is closed. Submission of BCQ is closed for <b>%s</b>."),
    INVALID_BCQ_VALUE("Invalid BCQ. BCQ value exceeds the configured maximum."),
    INVALID_TRADING_DATE("Trading date is invalid. Only one trading date can be accepted per file."),
    DIFFERENT_TRADING_DATE("Trading date is invalid. Trading date must be the same when overriding."),
    NEGATIVE_BCQ("BCQ is Negative. Declared BCQs should not be negative."),
    INVALID_BCQ_LENGTH("BCQ length is invalid. BCQ can only have a length of <b>19 for integer part</b>, "
            + "and <b>9 for fractional part</b>."),
    MISSING_INTERVAL("Missing interval."),
    MISSING_SELLING_MTN("Missing selling MTN."),
    MISSING_BUYER_MTN("Inconsistent buyer MTN."),
    MISSING_BILLING_ID("Missing billing ID."),
    MISSING_REFERENCE_MTN("Missing reference MTN."),
    MISSING_DATE("Missing date."),
    MISSING_BCQ("Missing BCQ."),
    INCOMPLETE_RESUBMISSION_ENTRIES("Incomplete BCQ entries. "
            + "Resubmission for date <b>%s</b> should also have entries of the ff. "
            + "Seller MTN and Billing ID pair(s):<br />%s"),
    INCOMPLETE_OVERRIDE_ENTRIES("Incomplete BCQ entries. "
            + "Override for date <b>%s</b> should also have entries of the ff. "
            + "Seller MTN and Billing ID pair(s):<br />%s"),
    SELLING_MTN_UNREGISTERED("Unregistered selling MTN. Seller MTN <b>%s</b> does not exist."),
    SELLING_MTN_NOT_OWNED("Unregistered selling MTN. "
            + "Seller MTN <b>%s</b> is not registered under Seller <b>%s (%s)</b>."),
        BUYER_MTN_UNREGISTERED("Unregistered buyer MTN. Buyer MTN <b>%s</b> is not registered under <b>%s</b>."),
    BUYER_MTN_NOT_OWNED("Unregistered buyer MTN. "
            + "buyer MTN <b>%s</b> is not registered under Buyer <b>%s (%s)</b>."),
    BILLING_ID_NOT_EXIST("Following billing ID(s) do not exist.<br />%s"),
    NO_ACTIVE_CONTRACT("Trading participant <b>%s (%s)</b> does not have active customer enrollment contract with "
            + "selling participant <b>%s (%s)</b> for trading date <b>%s</b>."),
    INDIRECT_NO_ACTIVE_CONTRACT("Indirect trading participant <b>%s (%s)</b> does not have active customer "
            + "enrollment contract with selling participant <b>%s (%s)</b> for trading date <b>%s</b>."),
    INDIRECT_AND_DIRECT_NO_ACTIVE_CONTRACT("Indirect trading participant <b>%s (%s)</b> and its "
            + "direct trading participant <b>%s (%s)</b> does not have active customer enrollment contract with "
            + "selling participant <b>%s (%s)</b> for trading date <b>%s</b>."),
    INDIRECT_NO_ACTIVE_CONTRACT_OR_COUNTERPARTY("Indirect trading participant <b>%s (%s)</b> does not have active "
            + "customer enrollment contract with or is not an indirect counterparty of selling participant <b>%s (%s)</b> "
            + "for trading date <b>%s</b>"),
    REFERENCE_MTN_UNREGISTERED("Unregistered reference MTN. Reference MTN <b>%s</b> does not exist."),
    REFERENCE_MTN_NOT_IN_CONTRACT("Unregistered reference MTN. "
            + "Reference MTN <b>%s</b> is not registered under contract of "
            + "Seller <b>%s (%s)</b> and Buyer <b>%s (%s)</b>."),
    REFERENCE_MTN_NOT_IN_COUNTERPARTY("Unregistered reference MTN. "
            + "Reference MTN <b>%s</b> is not registered under counterparty of "
            + "Seller <b>%s (%s)</b> and Buyer <b>%s (%s)</b>."),
    NO_SPECIAL_EVENT_FOUND(""),
    PARTICIPANTS_NOT_PRESENT_IN_SPECIAL_EVENT("Following participant(s) were not included in the special event for "
            + "the trading date of %s.<br />%s"),
    OVERRIDDEN_ENTRIES("Following entry(ies) have already been overridden for the trading date of %s.<br />%s"),
    DEADLINE_DATE_PASSED("Deadline date of special event for trading date <b>%s</b> and participants <b>%s</b> has "
            + "passed."),
    MULTIPLE_PARTICIPANT_BILLING_ID("Multiple participants are associated to billing ID <b>%s</b> for trading date <b>%s</b>."),
    CONTAINS_PROHIBITED_PAIRS("Declaration contains prohibited pairs. Declaration must not have entries of the ff. "
            + "Seller MTN and Billing ID pair(s):<br />%s"),
    BCQ_UPLOAD_SYS_CONFIG_ERROR("<b>%s</b> is not allowed in Bcq Upload"),
    BUYER_SELLER_MTN_SAME_FACILITY("Buyer Mtn <b>%s</b> is within the same facility with selling Mtn <b>%s</b>."),
    LOAD_OWN_BILLING_ID("Upload Error, CSV contains Trading Participant's own billing Id"),
    BUYER_DOEST_EXIST("Buyer participant does not exist!"),
    ERROR_OWN_FACILITY_VALIDATION("Error in validation of own facility"),
    REFERENCE_MTN_NOT_OWN("Reference MTN is not owned by Seller(%s (%s)) and/or its Indirect/s "),
    DUPLICATE_RECORD("Duplicate record on Seller MTN:(%s), Buyer Billing ID:(%s), Buyer MTN:(%s) for dispatch interval (%s)"),
    INCOMPLETE_MTN_COUNT("Must Declare All Mtn/s of the Buyer."),
    INVALID_BUYER_MTN_INTERVAL("Invalid/ Inconsistent Buyer Mtn. Found under "
                           + " <b>%s</b>, <b>%s</b>, <b>%s</b>"),
    BUYER_MTN_BILLING_ID_ERROR("Buyer Mtn <b>%s<b> doesn't have a Billing id of <b>%s</b>"),
    HOURLY_INTERVAL_ERROR("HOURLY interval must not have Buyer MTN"),
    MULTIPLE_TRADING_DATE("Multiple Trading date");


    private final String errorMessage;

    BcqValidationError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

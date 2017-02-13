package com.pemc.crss.metering.constants;

import java.util.Locale;

public enum BcqStatus {

    CANCELLED("Cancelled"),
    CONFIRMED("Confirmed"),
    NOT_CONFIRMED("Non Confirmed"),
    FOR_CONFIRMATION("For Confirmation"),
    NULLIFIED("Nullified"),
    NOT_NULLIFIED("Not Nullified"),
    FOR_NULLIFICATION("For Nullification"),
    FOR_APPROVAL_UPDATED("For Approval Updated"),
    FOR_APPROVAL_NEW("For Approval New"),
    FOR_APPROVAL_CANCEL("For Approval Cancel"),
    VOID("Void"),
    SETTLEMENT_READY("Settlement Ready");

    private final String label;

    BcqStatus(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public static BcqStatus fromString(String value) {
        try {
            return BcqStatus.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for BcqStatus enum", value), e);
        }
    }

    public static String getLabelFromStringValue(String value) {
        try {
            return BcqStatus.valueOf(value.toUpperCase(Locale.US)).getLabel();
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for BcqStatus enum", value), e);
        }
    }
}

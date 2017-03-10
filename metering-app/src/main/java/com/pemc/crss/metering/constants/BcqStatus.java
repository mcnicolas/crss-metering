package com.pemc.crss.metering.constants;

import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public enum BcqStatus {

    CANCELLED("Cancelled"),
    CONFIRMED("Confirmed"),
    NOT_CONFIRMED("Not Confirmed"),
    FOR_CONFIRMATION("For Confirmation"),
    NULLIFIED("Nullified"),
    FOR_NULLIFICATION("For Nullification"),
    FOR_APPROVAL_UPDATE("For Approval (Update)"),
    FOR_APPROVAL_NEW("For Approval (New)"),
    FOR_APPROVAL_CANCEL("For Approval (Cancel)"),
    VOID("Void"),
    SETTLEMENT_READY("Settlement Ready");

    private final String label;

    public static final List<BcqStatus> FOR_STATUSES = asList(FOR_CONFIRMATION, FOR_NULLIFICATION, FOR_APPROVAL_NEW,
            FOR_APPROVAL_UPDATE, FOR_APPROVAL_CANCEL);

    BcqStatus(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public static List<BcqStatus> getExcludedStatuses(boolean isSettlement) {
        if (isSettlement) {
            return singletonList(VOID);
        }
        return asList(VOID, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE);
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

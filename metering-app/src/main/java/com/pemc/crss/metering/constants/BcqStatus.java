package com.pemc.crss.metering.constants;

import java.util.Locale;

public enum BcqStatus {

    CANCELLED,
    CONFIRMED,
    NOT_CONFIRMED,
    FOR_CONFIRMATION,
    NULLIFIED,
    NOT_NULLIFIED,
    FOR_NULLIFICATION,
    FOR_APPROVAL_UPDATED,
    FOR_APPROVAL_NEW,
    FOR_APPROVAL_CANCEL;

    public static BcqStatus fromString(String value) {
        try {
            return BcqStatus.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for BcqStatus enum", value), e);
        }
    }
}

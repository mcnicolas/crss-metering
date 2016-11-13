package com.pemc.crss.metering.constants;

import java.util.Locale;

public enum BcqStatus {

    CONFIRMED,
    NOT_CONFIRMED,
    FOR_CONFIRMATION,
    NULLIFIED,
    NOT_NULLIFIED,
    FOR_NULLIFICATION;

    public static BcqStatus fromString(String value) {
        try {
            return BcqStatus.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for BCQStatus enum", value), e);
        }
    }
}

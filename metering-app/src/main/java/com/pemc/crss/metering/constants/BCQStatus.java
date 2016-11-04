package com.pemc.crss.metering.constants;

import java.util.Locale;

public enum BCQStatus {

    CONFIRMED,
    NOT_CONFIRMED,
    NULLIFIED,
    NOT_NULLIFIED;

    public static BCQStatus fromString(String value) {
        try {
            return BCQStatus.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for BCQStatus enum", value), e);
        }
    }
}

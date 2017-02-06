package com.pemc.crss.metering.constants;

import java.util.Locale;

public enum BcqUpdateType {

    RESUBMISSION,
    MANUAL_OVERRIDE;

    public static BcqUpdateType fromString(String value) {
        try {
            return BcqUpdateType.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for BcqUpdateType enum", value), e);
        }
    }

}

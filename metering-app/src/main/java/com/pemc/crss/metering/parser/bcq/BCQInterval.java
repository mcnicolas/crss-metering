package com.pemc.crss.metering.parser.bcq;

import com.pemc.crss.metering.parser.meterquantity.IntervalStatus;

import java.util.HashMap;
import java.util.Map;

public enum BCQInterval {

    HOURLY("Hourly"),
    QUARTERLY("15mins"),
    FIVE_MINUTES_PERIOD("5mins");

    private final String description;

    BCQInterval(String description) {
        this.description = description;
    }

    private static final Map<String, BCQInterval> INTERVAL_MAP = new HashMap<>();

    static {
        for (BCQInterval interval : BCQInterval.values()) {
            INTERVAL_MAP.put(interval.description, interval);
        }
    }

    public static BCQInterval fromDescription(String description) {
        BCQInterval interval = INTERVAL_MAP.get(description);

        return interval == null ? null : INTERVAL_MAP.get(description);
    }
}

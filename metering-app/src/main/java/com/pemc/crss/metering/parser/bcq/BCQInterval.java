package com.pemc.crss.metering.parser.bcq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public enum BCQInterval {

    HOURLY("Hourly", TimeUnit.MINUTES.toMillis(60)),
    QUARTERLY("15mins", TimeUnit.MINUTES.toMillis(15)),
    FIVE_MINUTES_PERIOD("5mins", TimeUnit.MINUTES.toMillis(5));

    private final String description;
    private final long timeInMillis;

    BCQInterval(String description, long timeInMillis) {
        this.description = description;
        this.timeInMillis = timeInMillis;
    }

    private static final Map<String, BCQInterval> INTERVAL_MAP = new HashMap<>();

    static {
        for (BCQInterval interval : BCQInterval.values()) {
            INTERVAL_MAP.put(interval.description, interval);
        }
    }

    public String getDescription() {
        return description;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public static BCQInterval fromDescription(String description) {
        BCQInterval interval = INTERVAL_MAP.get(description);

        return interval == null ? null : INTERVAL_MAP.get(description);
    }
}

package com.pemc.crss.metering.parser.bcq;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public enum BcqInterval {

    HOURLY("HOURLY", TimeUnit.MINUTES.toMillis(60), 24),
    QUARTERLY("15MINS", TimeUnit.MINUTES.toMillis(15), 96),
    FIVE_MINUTES_PERIOD("5MINS", TimeUnit.MINUTES.toMillis(5), 288);

    private final String description;
    private final long timeInMillis;
    private final int validNoOfRecords;

    BcqInterval(String description, long timeInMillis, int validNoOfRecords) {
        this.description = description;
        this.timeInMillis = timeInMillis;
        this.validNoOfRecords = validNoOfRecords;
    }

    private static final Map<String, BcqInterval> INTERVAL_MAP = new HashMap<>();

    static {
        for (BcqInterval interval : BcqInterval.values()) {
            INTERVAL_MAP.put(interval.description, interval);
        }
    }

    public String getDescription() {
        return description;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public int getValidNoOfRecords() {
        return validNoOfRecords;
    }

    public static BcqInterval fromDescription(String description) {
        return INTERVAL_MAP.get(description.toUpperCase());
    }

    public static String getValidIntervals() {
        List<String> validIntervals = new ArrayList<>();

        INTERVAL_MAP.entrySet().forEach(entry -> validIntervals.add(entry.getKey()));

        return StringUtils.join(validIntervals, ", ");
    }
}

package com.pemc.crss.metering.parser.meterquantity;

import java.util.HashMap;
import java.util.Map;

public enum IntervalStatus {
    NORMAL(0, "NORMAL", "Normal"),
    PO(1, "PO", "Power Outage"),
    SI(2, "SI", "Short Interval"),
    LI(3, "LI", "Long Interval"),
    CR(4, "CR", "CRC Error"),
    RA(5, "RA", "RAM Checksum Error"),
    RO(6, "RO", "ROM Checksum Error"),
    LA(7, "LA", "Data Missing"),
    CL(8, "CL", "Clock Error"),
    BR(9, "BR", "Reset Occurred"),
    WD(10, "WD", "Watchdog Time-out"),
    TR(11, "TR", "Time Reset Occurred"),
    TM(12, "TM", "Test Mode"),
    LC(13, "LC", "Load Control");

    private final int code;
    private final String shortName;
    private final String description;

    private static final Map<Integer, IntervalStatus> STATUS_MAP = new HashMap<>();

    static {
        for (IntervalStatus intervalStatus : IntervalStatus.values()) {
            STATUS_MAP.put(intervalStatus.code, intervalStatus);
        }
    }

    IntervalStatus(int code, String shortName, String description) {
        this.code = code;
        this.shortName = shortName;
        this.description = description;
    }

    public static IntervalStatus fromCode(int code) {
        return STATUS_MAP.get(code);
    }

    public int getCode() {
        return code;
    }

}

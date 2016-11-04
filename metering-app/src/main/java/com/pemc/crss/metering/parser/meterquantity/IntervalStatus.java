package com.pemc.crss.metering.parser.meterquantity;

import java.util.HashMap;
import java.util.Map;

public enum IntervalStatus {
    PO(0, "PO", "Power Outage"),
    SI(1, "SI", "Short Interval"),
    LI(2, "LI", "Long Interval"),
    CR(3, "CR", "CRC Error"),
    RA(4, "RA", "RAM Checksum Error"),
    RO(5, "RO", "ROM Checksum Error"),
    LA(6, "LA", "Data Missing"),
    CL(7, "CL", "Clock Error"),
    BR(8, "BR", "Reset Occurred"),
    WD(9, "WD", "Watchdog Time-out"),
    TR(10, "TR", "Time Reset Occurred"),
    TM(11, "TM", "Test Mode"),
    LC(12, "LC", "Load Control");

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

    public static String getShortName(int code) {
        IntervalStatus intervalStatus = STATUS_MAP.get(code);

        return intervalStatus == null ? "" : STATUS_MAP.get(code).shortName;
    }

}

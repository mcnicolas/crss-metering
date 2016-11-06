package com.pemc.crss.metering.parser.meterquantity;

import java.util.HashMap;
import java.util.Map;

public enum ChannelStatus {
    NORMAL(0, "NORMAL", "Normal"),
    UPD(1, "UPD", "Retransmitted / Updated Data"),
    AD(2, "AD", "Added Interval (Data Correction)"),
    RE(3, "RE", "Replaced Interval (Data Correction)"),
    ES(4, "ES", "Estimated Interval (Data Correction)"),
    POV(5, "POV", "Pulse Overflow"),
    DOV(6, "DOV", "Data Out Of Limits"),
    ED(7, "ED", "Excluded Data"),
    PE(8, "PE", "Parity"),
    ETC(9, "ETC", "Energy Type (Register Changed)"),
    LR(10, "LR", "Alarm"),
    HD(11, "HD", "Harmonic Distortion");

    private final int code;
    private final String shortName;
    private final String description;

    private static final Map<Integer, ChannelStatus> STATUS_MAP = new HashMap<>();

    static {
        for (ChannelStatus channelStatus : ChannelStatus.values()) {
            STATUS_MAP.put(channelStatus.code, channelStatus);
        }
    }

    ChannelStatus(int code, String shortName, String description) {
        this.code = code;
        this.shortName = shortName;
        this.description = description;
    }

    public static ChannelStatus fromCode(int code) {
        return STATUS_MAP.get(code);
    }

    public int getCode() {
        return code;
    }

}

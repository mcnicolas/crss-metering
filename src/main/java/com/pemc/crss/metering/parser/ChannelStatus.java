package com.pemc.crss.metering.parser;

import java.util.HashMap;
import java.util.Map;

public enum ChannelStatus {
    UPD(0, "UPD", "Retransmitted / Updated Data"),
    AD(1, "AD", "Added Interval (Data Correction)"),
    RE(2, "RE", "Replaced Interval (Data Correction)"),
    ES(3, "ES", "Estimated Interval (Data Correction)"),
    POV(4, "POV", "Pulse Overflow"),
    DOV(5, "DOV", "Data Out Of Limits"),
    ED(6, "ED", "Excluded Data"),
    PE(7, "PE", "Parity"),
    ETC(8, "ETC", "Energy Type (Register Changed)"),
    LR(9, "LR", "Alarm"),
    HD(10, "HD", "Harmonic Distortion");

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

    public static String getShortName(int code) {
        ChannelStatus channelStatus = STATUS_MAP.get(code);

        return channelStatus == null ? "" : STATUS_MAP.get(code).shortName;
    }

}

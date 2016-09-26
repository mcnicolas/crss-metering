package com.pemc.crss.metering.parser;

import java.util.HashMap;
import java.util.Map;

import static com.pemc.crss.metering.parser.ParserUtil.parseInt;

public enum RecordCode {
    METER_HEADER(1),
    CHANNEL_HEADER(10),
    INTERVAL_DATA(0),
    TRAILER_RECORD(9999);

    private int code;

    private static final Map<Integer, RecordCode> CODE_MAP = new HashMap<>();

    static {
        for (RecordCode recordCode : RecordCode.values()) {
            CODE_MAP.put(recordCode.code, recordCode);
        }
    }

    RecordCode(int code) {
        this.code = code;
    }

    public static RecordCode getRecordCode(byte[] buffer) {
        int recordCode = Integer.parseInt(parseInt(2, 3, buffer));

        // Special handling for interval data recordCode range
        if (recordCode >= 1001 && recordCode <= 9998) {
            recordCode = 0;
        }

        return CODE_MAP.get(recordCode);
    }

}

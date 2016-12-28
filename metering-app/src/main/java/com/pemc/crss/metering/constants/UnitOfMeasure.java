package com.pemc.crss.metering.constants;

import java.util.HashMap;
import java.util.Map;

public enum UnitOfMeasure {
    KWD("KW D"),
    KWHD("KWH D"),
    KVARHD("KVARH D"),
    KWR("KW R"),
    KWHR("KWH R"),
    KVARHR("KVARH R"),

    // NOTE: There are two formats for VAN. Handling them both.
    VAN1("VAN"),
    VAN2("V AN"),

    VBN("VBN"),
    VCN("VCN"),
    IAN("IA"),
    IBN("IB"),
    ICN("IC"),
    PF("PF");

    private String code;

    private static final Map<String, UnitOfMeasure> STATUS_MAP = new HashMap<>();

    static {
        for (UnitOfMeasure unitOfMeasure : UnitOfMeasure.values()) {
            STATUS_MAP.put(unitOfMeasure.code, unitOfMeasure);
        }
    }

    UnitOfMeasure(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UnitOfMeasure fromCode(String code) {
        return STATUS_MAP.get(code);
    }

}

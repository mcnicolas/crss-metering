package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.HashMap;
import java.util.Map;

public abstract class BcqSettlementEvent extends BcqEvent {

    public BcqSettlementEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source, eventCode);
    }

    protected abstract Map<String, Object> getPayload();

    protected Map<String, Object> getCommonPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("submittedDate", source.get("submittedDate"));
        payload.put("settlementUser", source.get("settlementUser"));
        return payload;
    }
}

package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.HashMap;
import java.util.Map;

public abstract class BcqStatusEvent extends BcqEvent {

    public BcqStatusEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source, eventCode);
    }

    protected abstract Map<String, Object> getPayload();

    protected Map<String, Object> getCommonPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradingDate", source.get("tradingDate"));
        payload.put("respondedDate", source.get("respondedDate"));
        payload.put("headerId", source.get("headerId"));
        return payload;
    }

}

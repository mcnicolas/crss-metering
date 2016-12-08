package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.HashMap;
import java.util.Map;

public abstract class BcqSubmitEvent extends BcqEvent {

    public BcqSubmitEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source, eventCode);
    }

    protected abstract Map<String, Object> getPayload();

    protected Map<String, Object> getCommonPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("submittedDate", source.get("submittedDate"));
        return payload;
    }

}

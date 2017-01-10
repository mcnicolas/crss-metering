package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

public abstract class BcqUnprocessedEvent extends BcqEvent {

    public BcqUnprocessedEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source, eventCode);
    }

    public Map<String, Object> getCommonPayload() {
        return of(
                "tradingDate", source.get("tradingDate"),
                "sellingMtns", source.get("sellingMtns"),
                "recipientId", source.get("recipientId"),
                "deadlineDate", source.get("deadlineDate"),
                "status", source.get("status")
        );
    }

}

package com.pemc.crss.metering.event;

import com.google.common.collect.ImmutableMap.Builder;
import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

public class BcqUnprocessedSellerEvent extends BcqUnprocessedEvent {

    public BcqUnprocessedSellerEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source, eventCode);
    }

    @Override
    protected Map<String, Object> getPayload() {
        return new Builder<String, Object>()
                .put("buyerName", source.get("buyerName"))
                .put("buyerShortName", source.get("buyerShortName"))
                .putAll(getCommonPayload())
                .build();
    }

}

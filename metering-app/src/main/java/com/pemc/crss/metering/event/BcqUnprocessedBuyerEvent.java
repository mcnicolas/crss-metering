package com.pemc.crss.metering.event;

import com.google.common.collect.ImmutableMap;
import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

public class BcqUnprocessedBuyerEvent extends BcqUnprocessedEvent {

    public BcqUnprocessedBuyerEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source, eventCode);
    }

    @Override
    protected Map<String, Object> getPayload() {
        return new ImmutableMap.Builder<String, Object>()
                .put("sellerName", source.get("sellerName"))
                .put("sellerShortName", source.get("sellerShortName"))
                .putAll(getCommonPayload())
                .build();
    }

}

package com.pemc.crss.metering.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_SUBMIT_BUYER;

@Slf4j
public class BcqSubmitBuyerEvent extends BcqSubmitEvent {

    public BcqSubmitBuyerEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_SUBMIT_BUYER);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("sellerName", source.get("sellerName"));
        payload.put("sellerShortName", source.get("sellerShortName"));
        payload.put("headerId", source.get("headerId"));
        return payload;
    }

}

package com.pemc.crss.metering.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_UPDATE_BUYER;

@Slf4j
public class BcqUpdateBuyerEvent extends BcqSubmitEvent {

    public BcqUpdateBuyerEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_UPDATE_BUYER);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("tradingDate", source.get("tradingDate"));
        payload.put("sellerName", source.get("sellerName"));
        payload.put("sellerShortName", source.get("sellerShortName"));
        payload.put("headerId", source.get("headerId"));
        return payload;
    }

}
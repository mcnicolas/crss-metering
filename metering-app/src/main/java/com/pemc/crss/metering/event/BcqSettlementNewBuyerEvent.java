package com.pemc.crss.metering.event;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_SETTLEMENT_NEW_BUYER;

public class BcqSettlementNewBuyerEvent extends BcqSettlementEvent {

    public BcqSettlementNewBuyerEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_SETTLEMENT_NEW_BUYER);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("sellerName", source.get("sellerName"));
        payload.put("sellerShortName", source.get("sellerShortName"));
        payload.put("recipientId", source.get("recipientId"));
        payload.put("headerId", source.get("headerId"));
        return payload;
    }

}

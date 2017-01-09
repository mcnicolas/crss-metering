package com.pemc.crss.metering.event;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_SETTLEMENT_UPDATE_DEPT;

public class BcqSettlementUpdateDeptEvent extends BcqSettlementEvent {

    public BcqSettlementUpdateDeptEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_SETTLEMENT_UPDATE_DEPT);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("tradingDate", source.get("tradingDate"));
        return payload;
    }

}

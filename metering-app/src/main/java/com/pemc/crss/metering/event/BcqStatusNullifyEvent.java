package com.pemc.crss.metering.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_NULLIFY_SELLER;

@Slf4j
public class BcqStatusNullifyEvent extends BcqStatusEvent {

    public BcqStatusNullifyEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_NULLIFY_SELLER);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("buyerName", source.get("buyerName"));
        payload.put("buyerShortName", source.get("buyerShortName"));
        return payload;
    }

}

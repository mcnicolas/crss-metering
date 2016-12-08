package com.pemc.crss.metering.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_SUBMIT_SELLER;

@Slf4j
public class BcqSubmitSellerEvent extends BcqSubmitEvent {

    public BcqSubmitSellerEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_SUBMIT_SELLER);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("recordCount", source.get("recordCount"));
        return payload;
    }

}

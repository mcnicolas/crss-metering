package com.pemc.crss.metering.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_VALIDATION_SELLER;

@Slf4j
public class BcqValidationSellerEvent extends BcqSubmitEvent {

    public BcqValidationSellerEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_VALIDATION_SELLER);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("errorMessage", source.get("errorMessage"));
        return payload;
    }

}

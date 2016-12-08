package com.pemc.crss.metering.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_VALIDATION_DEPT;

@Slf4j
public class BcqValidationDeptEvent extends BcqSubmitEvent {

    public BcqValidationDeptEvent(Map<String, Object> source) {
        super(source, NTF_BCQ_VALIDATION_DEPT);
    }

    @Override
    protected Map<String, Object> getPayload() {
        Map<String, Object> payload = getCommonPayload();
        payload.put("sellerName", source.get("sellerName"));
        payload.put("sellerShortName", source.get("sellerShortName"));
        payload.put("errorMessage", source.get("errorMessage"));
        return payload;
    }

}

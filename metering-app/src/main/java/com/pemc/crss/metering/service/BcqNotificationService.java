package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.BcqEventCode;
import com.pemc.crss.metering.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BcqNotificationService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void send(BcqEventCode code, Object... payloadObjects) {
        if (code.getPayloadNameList().size() != payloadObjects.length) {
            throw new IllegalArgumentException("Number of payload objects is incorrect.");
        }

        int index = 0;
        Map<String, Object> payload = new HashMap<>();
        for(String payloadName : code.getPayloadNameList()) {
            payload.put(payloadName, payloadObjects[index]);
            index ++;
        }

        eventPublisher.publishEvent(getEventByCode(code, payload));
    }

    private BcqEvent getEventByCode(BcqEventCode code, Map<String, Object> payload) {
        switch (code) {
            case NTF_BCQ_SUBMIT_BUYER:
                return new BcqSubmitBuyerEvent(payload);
            case NTF_BCQ_SUBMIT_SELLER:
                return new BcqSubmitSellerEvent(payload);
            case NTF_BCQ_VALIDATION_SELLER:
                return new BcqValidationSellerEvent(payload);
            case NTF_BCQ_VALIDATION_DEPT:
                return new BcqValidationDeptEvent(payload);
            case NTF_BCQ_CANCEL_BUYER:
                return new BcqStatusCancelEvent(payload);
            case NTF_BCQ_CONFIRM_SELLER:
                return new BcqStatusConfirmEvent(payload);
            case NTF_BCQ_NULLIFY_SELLER:
                return new BcqStatusNullifyEvent(payload);
            default:
                return null;
        }
    }

}

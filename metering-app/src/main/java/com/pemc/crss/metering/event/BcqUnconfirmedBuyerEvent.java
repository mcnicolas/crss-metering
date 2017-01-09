package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

public class BcqUnconfirmedBuyerEvent extends BcqUnprocessedBuyerEvent {

    public BcqUnconfirmedBuyerEvent(Map<String, Object> source) {
        super(source, BcqEventCode.NTF_BCQ_UNCONFIRMED_BUYER);
    }

}

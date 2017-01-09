package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

public class BcqUnnullifiedBuyerEvent extends BcqUnprocessedBuyerEvent {

    public BcqUnnullifiedBuyerEvent(Map<String, Object> source) {
        super(source, BcqEventCode.NTF_BCQ_UNNULLIFIED_BUYER);
    }

}

package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

public class BcqUnnullifiedSellerEvent extends BcqUnprocessedSellerEvent {

    public BcqUnnullifiedSellerEvent(Map<String, Object> source) {
        super(source, BcqEventCode.NTF_BCQ_UNNULLIFIED_SELLER);
    }

}

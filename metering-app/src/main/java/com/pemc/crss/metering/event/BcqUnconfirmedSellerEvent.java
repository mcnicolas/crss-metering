package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqEventCode;

import java.util.Map;

public class BcqUnconfirmedSellerEvent extends BcqUnprocessedSellerEvent {

    public BcqUnconfirmedSellerEvent(Map<String, Object> source) {
        super(source, BcqEventCode.NTF_BCQ_UNCONFIRMED_SELLER);
    }

}

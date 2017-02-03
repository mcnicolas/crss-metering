package com.pemc.crss.metering.dto.bcq.specialevent;

import lombok.Data;

import java.util.Date;

@Data
public class BcqEventValidationData {

    private String tradingParticipant;
    private Date tradingDate;
    private int tradingDateCount;

}

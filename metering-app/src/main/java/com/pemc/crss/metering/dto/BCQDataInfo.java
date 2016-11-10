package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BCQDataInfo {

    private String sellingMTN;
    private String buyingParticipant;
    private String referenceMTN;
    private Date startTime;
    private Date endTime;
    private float bcq;

}

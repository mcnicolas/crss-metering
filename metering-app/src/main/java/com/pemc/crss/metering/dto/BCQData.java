package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BCQData {

    private long bcqDataId;
    private long fileId;
    private String sellingMTN;
    private String buyingParticipant;
    private String referenceMTN;
    private Date startTime;
    private Date endTime;
    private double bcq;

}

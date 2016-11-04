package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BCQData {

    private long bcqDataId;
    private long fileId;
    private String sellingParticipantName;
    private String sellingMTN;
    private long buyingParticipantId;
    private String referenceMTN;
    private Date startTime;
    private Date endTime;
    private double bcq;

}

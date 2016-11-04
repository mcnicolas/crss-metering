package com.pemc.crss.metering.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BCQData {

    private long bcqDataId;
    private long fileId;
    private String sellingParticipantName;
    private String sellingMTN;
    private long buyingParticipantId;
    private String referenceMTN;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double bcq;

}

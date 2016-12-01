package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class BcqDeclarationDisplay {

    private long headerId;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private String sellingMtn;
    private String buyingParticipant;
    private String tradingDate;
    private String transactionID;
    private String submittedDate;
    private String updatedVia;
    private String status;

}

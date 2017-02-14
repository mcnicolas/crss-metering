package com.pemc.crss.metering.dto.bcq;

import lombok.Data;

@Data
public class BcqHeaderPageDisplay {

    private long headerId;
    private String sellingMtn;
    private String billingId;
    private long buyingParticipantUserId;
    private String buyingParticipantName;
    private String buyingParticipantShortName;
    private long sellingParticipantUserId;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private String tradingDate;
    private String transactionId;
    private String submittedDate;
    private String deadlineDate;
    private String status;
    private String updatedVia;

}

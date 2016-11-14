package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BcqHeaderInfo {

    private String sellingMTN;
    private String buyingParticipant;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private String status;
    private Date declarationDate;

}

package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.constants.BcqStatus;
import lombok.Data;

@Data
public class BcqHeaderInfo {

    private String sellingMTN;
    private String buyingParticipant;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private BcqStatus status;

}

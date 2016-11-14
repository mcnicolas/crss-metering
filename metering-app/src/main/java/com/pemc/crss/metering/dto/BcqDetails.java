package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class BcqDetails {

    private Long sellingParticipantId;
    private BcqUploadFileInfo fileInfo;
    private List<BcqHeaderDataInfoPair> headerDataInfoPairList;
    private Set<Long> buyingParticipantIds;

}

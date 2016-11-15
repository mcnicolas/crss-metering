package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BcqDetails {

    private Long sellerId;
    private BcqUploadFileInfo fileInfo;
    private List<BcqHeaderDataInfoPair> headerDataInfoPairList;
    private List<Long> buyerIds;

}

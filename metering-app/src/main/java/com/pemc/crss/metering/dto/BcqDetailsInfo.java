package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BcqDetailsInfo {

    private Long sellerId;
    private BcqUploadFileInfo fileInfo;
    private List<BcqHeaderInfo> headerInfoList;
    private List<Long> buyerIds;

}

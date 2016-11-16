package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BcqDetails {

    private Long sellerId;
    private BcqUploadFileInfo fileInfo;
    private List<BcqDeclarationInfo> bcqDeclarationInfoList;
    private List<Long> buyerIds;

}

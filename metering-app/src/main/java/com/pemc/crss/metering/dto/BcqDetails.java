package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class BcqDetails {

    private BcqUploadFileInfo fileInfo;
    private Map<BcqHeaderInfo, Set<BcqDataInfo>> headerDataMap;

}

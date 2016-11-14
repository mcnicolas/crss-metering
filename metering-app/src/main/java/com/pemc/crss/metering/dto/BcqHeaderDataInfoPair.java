package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BcqHeaderDataInfoPair {

    private BcqHeaderInfo headerInfo;
    private List<BcqDataInfo> dataInfoList;

}

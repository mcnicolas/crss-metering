package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BCQDetails {

    private BCQUploadFileInfo fileInfo;
    private List<BCQDataInfo> dataInfoList;

}

package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class BCQUploadFileInfo {

    private String fileName;
    private long fileSize;
    private String status;

}

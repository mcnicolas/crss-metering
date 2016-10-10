package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class MeterUploadMDEF {

    private long fileID;
    private long transactionID;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String status;

}

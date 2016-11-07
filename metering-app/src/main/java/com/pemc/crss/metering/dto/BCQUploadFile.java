package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.constants.BCQStatus;
import lombok.Data;

@Data
public class BCQUploadFile {

    private long fileID;
    private String transactionID;
    private String fileName;
    private long fileSize;
    private BCQStatus status;

}

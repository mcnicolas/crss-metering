package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.constants.ValidationStatus;
import lombok.Data;

@Data
public class MeterUploadFile {

    private long fileID;
    private long transactionID;
    private String fileName;
    private FileType fileType;
    private long fileSize;
    private ValidationStatus status;

}

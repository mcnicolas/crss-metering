package com.pemc.crss.metering.dto.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.constants.UploadType;
import lombok.Data;

@Data
public class FileManifest {

    private long headerID;
    private String transactionID;
    private long fileID;
    private String fileName;
    private FileType fileType;
    private long fileSize;
    private String checksum;
    private String recvChecksum;
    private String mspShortName;
    private UploadType uploadType;
    private String uploadDateTime;
    private String processFlag;
    private String status;
    private String errorDetails;

}

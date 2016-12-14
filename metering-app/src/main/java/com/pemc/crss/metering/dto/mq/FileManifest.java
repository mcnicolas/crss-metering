package com.pemc.crss.metering.dto.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.constants.UploadType;
import lombok.Data;

import java.util.Date;

@Data
public class FileManifest {

    private long headerID;
    private String transactionID;
    private long fileID;
    private String fileName;
    private FileType fileType;
    private long fileSize;
    private String checksum;
    private String mspShortName;

    // TODO: Rename to categoryType
    private UploadType uploadType;
    private Date uploadDateTime;
    private String processFlag;
    private String status;
    private String errorDetails;

}

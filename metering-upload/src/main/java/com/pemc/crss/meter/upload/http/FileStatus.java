package com.pemc.crss.meter.upload.http;

import lombok.Data;

@Data
public class FileStatus {

    private long headerID;
    private String transactionID;
    private long fileID;
    private String fileName;

    private String status;
    private String errorDetails;

}

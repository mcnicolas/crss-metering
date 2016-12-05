package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BcqUploadFile {

    private long fileID;
    private String transactionID;
    private String fileName;
    private long fileSize;
    private Date submittedDate;

}

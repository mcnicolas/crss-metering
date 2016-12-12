package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BcqUploadFile {

    private long fileId;
    private String transactionId;
    private String fileName;
    private long fileSize;
    private Date submittedDate;

}

package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MeterUploadHeader {

    private long transactionID;
    private long mspID;
    private String category;
    private String notificationSent;
    private String notificationDateTime;
    private String uploadedBy;
    private Date uploadedDateTime;
    private int version;

}

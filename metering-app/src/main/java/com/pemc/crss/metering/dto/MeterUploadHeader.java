package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class MeterUploadHeader {

    private long transactionID;
    private long mspID;
    private String category;
    private String notificationSent;
    private String notificationDateTime;
    private String uploadedBy;
    private String uploadedDateTime;
    private int version;

}

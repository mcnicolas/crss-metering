package com.pemc.crss.meter.upload.http;

import lombok.Data;

import java.util.Date;

@Data
public class HeaderStatus {

    private long headerID;
    private String transactionID;
    private int fileCount;
    private String notificationSent;
    private Date notificationDateTime;
    private Date uploadDateTime;

}

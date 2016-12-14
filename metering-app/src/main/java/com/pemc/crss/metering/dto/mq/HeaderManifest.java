package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.util.Date;

@Data
public class HeaderManifest {

    private long headerID;
    private String transactionID;
    private int fileCount;
    private String category;
    private String notificationSent;
    private String notificationDateTime;
    private String tailReceived;
    private String uploadedBy;
    private Date uploadDateTime;

}

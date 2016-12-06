package com.pemc.crss.metering.dto.mq;

import lombok.Data;

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
    private String uploadDateTime;

}

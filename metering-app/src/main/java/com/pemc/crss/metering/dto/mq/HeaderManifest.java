package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.util.Date;

@Data
public class HeaderManifest {

    private long headerID;
    private String transactionID;
    private String mspShortName;
    private int fileCount;
    private String category;
    private String notificationSent;
    private Date notificationDateTime;
    private String tailReceived;
    private String uploadedBy;
    private Date uploadDateTime;
    private String convertedToFiveMin;
    private String closureTimeSetting;
    private String allowableDateSetting;

}

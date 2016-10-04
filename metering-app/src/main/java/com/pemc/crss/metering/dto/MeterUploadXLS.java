package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class MeterUploadXLS {

    private long meterDataID;
    private long fileID;
    private String customerID;
    private String readingDateTime;
    private String meterNo;
    private String channelStatus;
    private String channelStatusDesc;
    private String intervalStatus;
    private String intervalStatusDesc;

}

package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MeterDataDisplay {

    private long meterDataID;
    private String sein;
//    private Date readingDateTime;
    private long readingDateTime;
    private String kwd;
    private String kwhd;
    private String kvarhd;
    private String kwr;
    private String kwhr;
    private String kvarhr;
    private String estimationFlag;
    private int version;

}

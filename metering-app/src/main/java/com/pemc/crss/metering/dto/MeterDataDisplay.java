package com.pemc.crss.metering.dto;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class MeterDataDisplay {

    private static final DateFormat readingDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    private long meterDataID;
    private String transactionID;
    private String sein;
    private Date readingDateTime;
    private String kwd;
    private String kwhd;
    private String kvarhd;
    private String kwr;
    private String kwhr;
    private String kvarhr;
    private String estimationFlag;
    private int version;

    public String getReadingDateDisplay() {
        return readingDateFormat.format(readingDateTime);
    }

}

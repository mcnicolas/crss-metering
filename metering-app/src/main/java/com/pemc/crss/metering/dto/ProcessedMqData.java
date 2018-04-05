package com.pemc.crss.metering.dto;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Data
public class ProcessedMqData {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    private String category;

    private String mspShortname;

    private String sein;

    private String readingDateTime;

    private Object kwhd;

    private Object kvarhd;

    private Object kwd;

    private Object kwhr;

    private Object kvarhr;

    private Object kwr;

    private String estimationFlag;

    private String uploadDateTime;

    private String transactionId;

}

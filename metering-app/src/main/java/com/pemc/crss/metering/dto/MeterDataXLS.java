package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MeterDataXLS {

    private String sein;
    private Date readingDateTime;
    private double kwd;
    private double kwhd;
    private double kvarhd;
    private double kwr;
    private double kwhr;
    private double kvarhr;

}

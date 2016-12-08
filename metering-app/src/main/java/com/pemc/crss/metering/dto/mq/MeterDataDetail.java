package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.util.Date;

@Data
public class MeterDataDetail {

    private long meterDataID;
    private String sein;
    private Date readingDateTime;
    private int interval;

    private Double kwd;
    private int kwdChannelStatus;
    private int kwdIntervalStatus;
    private Double kwhd;
    private int kwhdChannelStatus;
    private int kwhdIntervalStatus;
    private Double kvarhd;
    private int kvarhdChannelStatus;
    private int kvarhdIntervalStatus;
    private Double kwr;
    private int kwrChannelStatus;
    private int kwrIntervalStatus;
    private Double kwhr;
    private int kwhrChannelStatus;
    private int kwhrIntervalStatus;
    private Double kvarhr;
    private int kvarhrChannelStatus;
    private int kvarhrIntervalStatus;
    private Double van;
    private int vanChannelStatus;
    private int vanIntervalStatus;
    private Double vbn;
    private int vbnChannelStatus;
    private int vbnIntervalStatus;
    private Double vcn;
    private int vcnChannelStatus;
    private int vcnIntervalStatus;
    private Double ian;
    private int ianChannelStatus;
    private int ianIntervalStatus;
    private Double ibn;
    private int ibnChannelStatus;
    private int ibnIntervalStatus;
    private Double icn;
    private int icnChannelStatus;
    private int icnIntervalStatus;
    private Double pf;
    private int pfChannelStatus;
    private int pfIntervalStatus;
    private String estimationFlag;
    private int version;

}
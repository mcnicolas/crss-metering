package com.pemc.crss.metering.dto.mq;

import com.pemc.crss.metering.constants.UploadType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MeterDataDetail {

    private long meterDataID;
    private long fileID;
    private String sein;
    private Long readingDateTime;
    private UploadType uploadType;
    private int interval;
    private String mspShortName;

    private BigDecimal kwd;
    private int kwdChannelStatus;
    private int kwdIntervalStatus;
    private BigDecimal kwhd;
    private int kwhdChannelStatus;
    private int kwhdIntervalStatus;
    private BigDecimal kvarhd;
    private int kvarhdChannelStatus;
    private int kvarhdIntervalStatus;
    private BigDecimal kwr;
    private int kwrChannelStatus;
    private int kwrIntervalStatus;
    private BigDecimal kwhr;
    private int kwhrChannelStatus;
    private int kwhrIntervalStatus;
    private BigDecimal kvarhr;
    private int kvarhrChannelStatus;
    private int kvarhrIntervalStatus;
    private BigDecimal van;
    private int vanChannelStatus;
    private int vanIntervalStatus;
    private BigDecimal vbn;
    private int vbnChannelStatus;
    private int vbnIntervalStatus;
    private BigDecimal vcn;
    private int vcnChannelStatus;
    private int vcnIntervalStatus;
    private BigDecimal ian;
    private int ianChannelStatus;
    private int ianIntervalStatus;
    private BigDecimal ibn;
    private int ibnChannelStatus;
    private int ibnIntervalStatus;
    private BigDecimal icn;
    private int icnChannelStatus;
    private int icnIntervalStatus;
    private BigDecimal pf;
    private int pfChannelStatus;
    private int pfIntervalStatus;
    private String estimationFlag;
    private Date createdDateTime;

}

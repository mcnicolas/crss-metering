package com.pemc.crss.metering.dto.mq;

import com.pemc.crss.metering.constants.UploadType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class MeterDataDetail {

    private long meterDataID;
    private long fileID;
    private String sein;
    private Long readingDateTime;
    private UploadType uploadType;
    private int interval;
    private String mspShortName;

    private BigDecimal kwd;
    private Integer kwdChannelStatus;
    private Integer kwdIntervalStatus;
    private BigDecimal kwhd;
    private Integer kwhdChannelStatus;
    private Integer kwhdIntervalStatus;
    private BigDecimal kvarhd;
    private Integer kvarhdChannelStatus;
    private Integer kvarhdIntervalStatus;
    private BigDecimal kwr;
    private Integer kwrChannelStatus;
    private Integer kwrIntervalStatus;
    private BigDecimal kwhr;
    private Integer kwhrChannelStatus;
    private Integer kwhrIntervalStatus;
    private BigDecimal kvarhr;
    private Integer kvarhrChannelStatus;
    private Integer kvarhrIntervalStatus;
    private BigDecimal van;
    private Integer vanChannelStatus;
    private Integer vanIntervalStatus;
    private BigDecimal vbn;
    private Integer vbnChannelStatus;
    private Integer vbnIntervalStatus;
    private BigDecimal vcn;
    private Integer vcnChannelStatus;
    private Integer vcnIntervalStatus;
    private BigDecimal ian;
    private Integer ianChannelStatus;
    private Integer ianIntervalStatus;
    private BigDecimal ibn;
    private Integer ibnChannelStatus;
    private Integer ibnIntervalStatus;
    private BigDecimal icn;
    private Integer icnChannelStatus;
    private Integer icnIntervalStatus;
    private BigDecimal pf;
    private Integer pfChannelStatus;
    private Integer pfIntervalStatus;
    private String estimationFlag;
    private Date createdDateTime;

    public MeterDataDetail(MeterDataDetail meterDataDetail) {
        this.meterDataID = meterDataDetail.meterDataID;
        this.fileID = meterDataDetail.fileID;
        this.sein = meterDataDetail.sein;
        this.readingDateTime = meterDataDetail.readingDateTime;
        this.uploadType = meterDataDetail.uploadType;
        this.interval = meterDataDetail.interval;
        this.mspShortName = meterDataDetail.mspShortName;
        this.estimationFlag = meterDataDetail.estimationFlag;
        this.createdDateTime = meterDataDetail.createdDateTime;
    }

}

package com.pemc.crss.metering.service.reports.dto;

import com.pemc.crss.commons.reports.ReportBean;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
public class BcqDataReportBean implements ReportBean {

    private String sellingParticipant;

    private String buyingParticipant;

    private String sellingMtn;

    private String billingId;

    private Date tradingDate;

    private String transactionId;

    private Timestamp submittedDate;

    private Date deadlineDate;

    private String status;

    private String updatedVia;

    private String referenceMtn;

    private Timestamp endTime;

    private BigDecimal bcq;

}

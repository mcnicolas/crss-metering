package com.pemc.crss.metering.service.reports.dto;

import com.pemc.crss.commons.reports.AbstractReportBuilder;
import com.pemc.crss.commons.reports.ReportBean;
import com.pemc.crss.metering.utils.BcqDateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BcqDataReportBuilder extends AbstractReportBuilder {

    public BcqDataReportBuilder(final List<ReportBean> reportBeans){
        super(reportBeans);
    }

    @Override
    protected String[] getHeaders() {
        return new String[] {
            "Selling Participant",
            "Buying Participant",
            "Selling MTN",
            "Buyer Billing ID",
            "Trading Date",
            "Transaction ID",
            "Submitted Date / Time",
            "Deadline Date",
            "Status",
            "Updated Via",
            "Reference MTN",
            "Time",
            "BCQ"
        };
    }

    @Override
    protected String[] getFieldMapper() {
        return new String[] {
            "sellingParticipant",
            "buyingParticipant",
            "sellingMtn",
            "billingId",
            "tradingDate",
            "transactionId",
            "submittedDate",
            "deadlineDate",
            "status",
            "updatedVia",
            "referenceMtn",
            "endTime",
            "bcq"
        };
    }

    @Override
    protected CellProcessor[] getProcessors() {
        return new CellProcessor[] {
            new NotNull(),
            new NotNull(),
            new NotNull(),
            new NotNull(),
            new FmtDate(BcqDateUtils.DATE_FORMAT),
            new NotNull(),
            new FmtDate(BcqDateUtils.DATE_TIME_FORMAT),
            new FmtDate(BcqDateUtils.DATE_FORMAT),
            new NotNull(),
            new Optional(),
            new NotNull(),
            new FmtDate(BcqDateUtils.DATE_TIME_FORMAT),
            new NotNull()
        };
    }
}

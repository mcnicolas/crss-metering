package com.pemc.crss.metering.dto.bcq.mapper;

import com.pemc.crss.commons.reports.ReportBean;
import com.pemc.crss.metering.service.reports.dto.BcqDataReportBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BcqDataReportMapper implements RowMapper<ReportBean> {

    @Override
    public BcqDataReportBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        BcqDataReportBean report = new BcqDataReportBean();

        report.setSellingParticipant(rs.getString("selling_participant"));
        report.setBuyingParticipant(rs.getString("buying_participant"));
        report.setSellingMtn(rs.getString("selling_mtn"));
        report.setBillingId(rs.getString("billing_id"));
        report.setTradingDate(rs.getDate("trading_date"));
        report.setTransactionId(rs.getString("transaction_id"));
        report.setSubmittedDate(rs.getTimestamp("submitted_date"));
        report.setDeadlineDate(rs.getDate("deadline_date"));
        report.setStatus(rs.getString("status"));
        report.setUpdatedVia(rs.getString("updated_via"));
        report.setReferenceMtn(rs.getString("reference_mtn"));
        report.setEndTime(rs.getTimestamp("end_time"));
        report.setBcq(rs.getBigDecimal("bcq"));

        return report;
    }
}

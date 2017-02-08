package com.pemc.crss.commons.reports;

import lombok.Data;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.List;

@Data
public abstract class AbstractReportBuilder {

    List<ReportBean> reportBeans;

    protected abstract String[] getHeaders();

    protected abstract String[] getFieldMapper();

    protected abstract CellProcessor[] getProcessors();
}

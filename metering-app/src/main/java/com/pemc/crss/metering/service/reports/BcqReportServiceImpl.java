package com.pemc.crss.metering.service.reports;

import com.pemc.crss.commons.reports.ReportBean;
import com.pemc.crss.commons.reports.ReportCsvWriter;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.service.reports.dto.BcqDataReportBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqReportServiceImpl implements BcqReportService {

    private final BcqDao bcqDao;

    @Override
    public void generateBcqDataReport(final Map<String, String> mapParams, final OutputStream outputStream) {
        final List<ReportBean> reportData = bcqDao.queryBcqDataReport(mapParams);
        ReportCsvWriter.write(new BcqDataReportBuilder(reportData), outputStream);
    }
}

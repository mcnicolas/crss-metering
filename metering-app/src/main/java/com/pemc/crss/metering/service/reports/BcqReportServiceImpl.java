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
import java.util.LinkedList;
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

    @Override
    public void generateBcqUploadTemplate(final OutputStream outputStream) {
        List<String[]> headerList = new LinkedList<>();
        String[] firstLine = {"Interval","<Hourly/5mins/15mins>"};
        String[] secondLine = {"Selling MTN (Resource ID)","Buying Participant (Load Participant Name)",
                "Reference MTN (Resource ID)","Date","BCQ",};
        String[] thirdLine = {"<text>","<text>","<text>","<date with format yyyy-mm-dd hh:mm>","<numeric>"};
        String[] fourthLine = {"MTN_TRADE_1","ares","MTN_TRADE_1","2017-02-15 01:00","10"};
        headerList.add(firstLine);
        headerList.add(secondLine);
        headerList.add(thirdLine);
        headerList.add(fourthLine);

        ReportCsvWriter.writeHeaders(headerList, outputStream);
    }
}

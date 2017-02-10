package com.pemc.crss.metering.service.reports;

import java.io.OutputStream;
import java.util.Map;

public interface BcqReportService {

    void generateBcqDataReport(Map<String, String> mapParams, OutputStream outputStream);

}

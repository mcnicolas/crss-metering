package com.pemc.crss.metering.service.reports;

import java.io.OutputStream;
import java.util.Map;

public interface BcqReportService {

    void generateDataReport(Map<String, String> mapParams, OutputStream outputStream);

    void generateTemplate(OutputStream outputStream);

}

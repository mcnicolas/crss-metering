package com.pemc.crss.metering.resource.mq_data.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionHeader;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionMeterData;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionMeterReading;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
usage:
       curl -X GET http://localhost:8080/metering/mq-data/extraction/daily/NGCP/TESTSEIN1/ALL/2017-08-11 -H 'Authorization: Bearer 3f7f4e1e-3321-48ee-a16d-e9cd39726e2a' -O -J

 */

@Slf4j
@RestController
@RequestMapping("mq-data/extraction/{tpShortName}/{sein}/{isLatest}")
public class MqDataExtractionResource {
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final ObjectMapper objectMapper;
    private final MeterService meterService;

    public MqDataExtractionResource(MeterService meterService) {
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
        this.meterService = meterService;
    }

    @GetMapping("/{category}/{tradingDate}")
    public void extractMqData(@PathVariable("category") String category,
                              @PathVariable("tpShortName") String tpShortName,
                              @PathVariable("sein") String sein,
                              @PathVariable("isLatest") String isLatest,
                              @PathVariable("tradingDate") String tradingDate,
                              HttpServletResponse response) throws Exception {
        log.debug("received extract {} mq data request :: tpShortName=[{}], sein=[{}], isLatest=[{}], tradingDate=[{}]",
                category, tpShortName, sein, isLatest, tradingDate);
        switch (category.toUpperCase()) {
            case "DAILY":
                break;
            case "MONTHLY":
                break;
            default:
                throw new RuntimeException("Invalid meter data category " + category);
        }

        MqExtractionHeader header = processMqReport(category, sein, tpShortName, tradingDate, "LATEST".equalsIgnoreCase(isLatest));

        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(header);
        String fileName = URLEncoder.encode(String.format("MQ_%s_%s_%s_%s_%s.json",
                tpShortName,
                category.toUpperCase(),
                sein,
                tradingDate.replaceAll("-", ""),
                DATETIME_FORMAT.format(LocalDateTime.now())
        ), "UTF-8");
        fileName = URLDecoder.decode(fileName, "ISO8859_1");
        response.setContentType("application/x-msdownload");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.addHeader("Content-disposition", "attachment; filename=" + fileName);

        try (OutputStream os = response.getOutputStream()) {
            os.write(result.getBytes(Charset.forName("UTF-8")));
        }
    }

    private MqExtractionHeader processMqReport(String category,
                                               String sein,
                                               String tpShortName,
                                               String tradingDate,
                                               boolean isLatest) throws Exception {
        List<ProcessedMqData> resultList =
                meterService.getMeterDataForExtraction(category, sein, tpShortName, tradingDate, null, isLatest);

        if (CollectionUtils.isEmpty(resultList)) {
            throw new Exception("No meter data found for given parameters");
        }

        Map<String, MqExtractionMeterData> mqExtractionMeterDataMap = new HashMap<>();

        for (ProcessedMqData processedMqData : resultList) {
            String dataKey = processedMqData.getUploadDateTime() + processedMqData.getTransactionId();
            List<MqExtractionMeterReading> readings;
            MqExtractionMeterData meterData;

            if (mqExtractionMeterDataMap.containsKey(dataKey)) {
                meterData = mqExtractionMeterDataMap.get(dataKey);
            } else {
                meterData = new MqExtractionMeterData(processedMqData.getUploadDateTime(),
                        processedMqData.getTransactionId());
                mqExtractionMeterDataMap.put(dataKey, meterData);
            }

            readings = meterData.getMeterReadings();
            MqExtractionMeterReading reading = new MqExtractionMeterReading(
                    processedMqData.getReadingDateTime(),
                    processedMqData.getKwhd(),
                    processedMqData.getKvarhd(),
                    processedMqData.getKwd(),
                    processedMqData.getKwhr(),
                    processedMqData.getKvarhr(),
                    processedMqData.getKwr(),
                    processedMqData.getEstimationFlag()
            );
            BeanUtils.copyProperties(processedMqData, reading);
            readings.add(reading);
        }

        return new MqExtractionHeader(category.toUpperCase(), sein, resultList.get(0).getMspShortname(), tradingDate,
                new ArrayList<>(mqExtractionMeterDataMap.values()));
    }
}

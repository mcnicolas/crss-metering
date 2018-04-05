package com.pemc.crss.metering.resource.mq_data.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionDailyHeader;
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

    private final ObjectMapper objectMapper;
    private final MeterService meterService;

    public MqDataExtractionResource(MeterService meterService) {
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
        this.meterService = meterService;
    }

    @GetMapping("/daily/{tradingDate}")
    public void extractDailyMqData(@PathVariable("tpShortName") String tpShortName,
                                   @PathVariable("sein") String sein,
                                   @PathVariable("isLatest") String isLatest,
                                   @PathVariable("tradingDate") String tradingDate,
                                   HttpServletResponse response) throws Exception {
        log.debug("received extract daily mq data request :: tpShortName=[{}], sein=[{}], isLatest=[{}], tradingDate=[{}]",
                tpShortName, sein, isLatest, tradingDate);

        List<ProcessedMqData> resultList =
                meterService.getMeterDataForExtraction("daily", sein, tpShortName, tradingDate, null, true);

        if (CollectionUtils.isEmpty(resultList)) {
            throw new Exception(String.format("No meter data found for parameters :: tpShortName=[%s], sein=[%s], tradingDate=[%s]",
                    tpShortName, sein, tradingDate));
        }

        MqExtractionDailyHeader header = new MqExtractionDailyHeader(resultList.get(0).getMspShortname(), sein, tradingDate);
        Map<String, MqExtractionMeterData> mqExtractionMeterDataMap = new HashMap<>();

        for(ProcessedMqData processedMqData: resultList){
            String dataKey = processedMqData.getUploadDateTime() + processedMqData.getTransactionId();
            List<MqExtractionMeterReading> readings;
            MqExtractionMeterData meterData;

            if(mqExtractionMeterDataMap.containsKey(dataKey)) {
                meterData = mqExtractionMeterDataMap.get(dataKey);
            } else {
                 meterData = new MqExtractionMeterData(processedMqData.getUploadDateTime(),
                        processedMqData.getTransactionId());
                mqExtractionMeterDataMap.put(dataKey, meterData);
            }

            readings = meterData.getMeterReadings();
            MqExtractionMeterReading reading = new MqExtractionMeterReading();
            BeanUtils.copyProperties(processedMqData, reading);
            readings.add(reading);
        }

        header.setMeterData(new ArrayList<>(mqExtractionMeterDataMap.values()));
        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(header);
        String fileName = URLEncoder.encode(sein + "_" + tradingDate + "_" + System.currentTimeMillis() + ".json", "UTF-8");
        fileName = URLDecoder.decode(fileName, "ISO8859_1");
        response.setContentType("application/x-msdownload");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.addHeader("Content-disposition", "attachment; filename=" + fileName);

        try (OutputStream os = response.getOutputStream()) {
            os.write(result.getBytes(Charset.forName("UTF-8")));
        }
    }


    @GetMapping("/monthly/{startDate}-{endDate}")
    public void extractMontlyMqData(@PathVariable("tpShortName") String tpShortName,
                                    @PathVariable("sein") String sein,
                                    @PathVariable("isLatest") String isLatest,
                                    @PathVariable("startDate") String startDate,
                                    @PathVariable("endDate") String endDate) {
        log.debug("received extract daily mq data request :: tpShortName=[{}], sein=[{}], isLatest=[{}], startDate=[{}], endDate=[{}]",
                tpShortName, sein, isLatest, startDate, endDate);
    }
}

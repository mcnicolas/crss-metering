package com.pemc.crss.metering.resource.mq_data.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pemc.crss.metering.dao.UserTpDao;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MissingParametersDto;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionHeader;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionMeterData;
import com.pemc.crss.metering.resource.mq_data.extraction.dto.MqExtractionMeterReading;
import com.pemc.crss.metering.service.MeterService;
import com.pemc.crss.metering.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/*
usage:
       curl -X GET http://localhost:8080/metering/mq-data/extraction?category=daily&sein=MF3MPNTNGCP01&isLatest=ALL&tradingDate=2017-08-11
       -H 'Authorization: Bearer 3f7f4e1e-3321-48ee-a16d-e9cd39726e2a' -O -J

 */

@Slf4j
@RestController
@RequestMapping("/mq-data/extraction")
public class MqDataExtractionResource {
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final ObjectMapper objectMapper;
    private final MeterService meterService;
    private final UserTpDao userTpDao;

    public MqDataExtractionResource(MeterService meterService, UserTpDao userTpDao) {
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
        this.meterService = meterService;
        this.userTpDao = userTpDao;
    }

    @GetMapping
    public void extractMqData(@RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "sein", required = false) String sein,
                              @RequestParam(value = "isLatest", required = false) String isLatest,
                              @RequestParam(value = "tradingDate", required = false) String tradingDate,
                              HttpServletResponse response) throws Exception {
        log.debug("received extract {} mq data request ::  sein=[{}], isLatest=[{}], tradingDate=[{}]",
                category, sein, isLatest, tradingDate);

        String result;
        String fileName;
        if (StringUtils.isBlank(category)
                || StringUtils.isBlank(sein)
                || StringUtils.isBlank(isLatest)
                || StringUtils.isBlank(tradingDate)) {
            result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    new MissingParametersDto(HttpStatus.BAD_REQUEST, "/mq-data/extraction")
                            .addToMissingParams("category", category)
                            .addToMissingParams("sein", sein)
                            .addToMissingParams("isLatest", isLatest)
                            .addToMissingParams("tradingDate", tradingDate));
            response.setStatus(400);
            fileName = URLDecoder.decode(URLEncoder.encode(String.format("missing_fields_%d", System.currentTimeMillis())
                    , "UTF-8"), "ISO8859_1");
        } else {


            log.info("userId={}", SecurityUtils.getUserId());

            String tpShortName = userTpDao.findBShortNameByTpId(SecurityUtils.getUserId().longValue());

            switch (isLatest.toUpperCase()) {
                case "ALL":
                case "LATEST":
                    break;
                default:
                    throw new IllegalArgumentException("Invalid version " + isLatest);
            }

            switch (category.toUpperCase()) {
                case "DAILY":
                case "MONTHLY":
                    break;
                default:
                    throw new IllegalArgumentException("Invalid meter data category " + category);
            }

            MqExtractionHeader header = processMqReport(category, sein, tpShortName, tradingDate, "LATEST".equalsIgnoreCase(isLatest));

            result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(header);
            fileName = URLEncoder.encode(String.format("MQ_%s_%s_%s_%s_%s_%s.json",
                    tpShortName,
                    category.toUpperCase(),
                    isLatest.toUpperCase(),
                    sein,
                    tradingDate.replaceAll("-", ""),
                    DATETIME_FORMAT.format(LocalDateTime.now())
            ), "UTF-8");
            fileName = URLDecoder.decode(fileName, "ISO8859_1");
        }

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
                StringUtils.isNotBlank(tpShortName)
                        ? meterService.getMeterDataForExtraction(category, sein, tpShortName, tradingDate, null, isLatest)
                        : Collections.emptyList();

        if (CollectionUtils.isEmpty(resultList)) {
            log.info("No meter data found for given parameters");
            return new MqExtractionHeader(category.toUpperCase(), sein, null, tradingDate, null);
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

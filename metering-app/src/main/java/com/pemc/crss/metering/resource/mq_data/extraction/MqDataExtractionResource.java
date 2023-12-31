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
import com.pemc.crss.shared.commons.util.reference.Function;
import com.pemc.crss.shared.commons.util.reference.Module;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.pemc.crss.shared.commons.util.AuditUtil.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


/*
usage:
       curl -X GET http://localhost:8080/metering/mq-data/extraction?category=daily&sein=MF3MPNTNGCP01&isLatest=ALL&tradingDate=2017-08-11
       -H 'Authorization: Bearer 3f7f4e1e-3321-48ee-a16d-e9cd39726e2a' -O -J

 */

@Slf4j
@RestController
@RequestMapping("/mq-data/extraction")
public class MqDataExtractionResource {

    private static final String DAILY = "DAILY";
    private static final String MONTHLY = "MONTHLY";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final ObjectMapper objectMapper;
    private final MeterService meterService;
    private final UserTpDao userTpDao;
    private final RedisTemplate genericRedisTemplate;

    public MqDataExtractionResource(MeterService meterService, UserTpDao userTpDao, RedisTemplate genericRedisTemplate) {
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
        this.meterService = meterService;
        this.userTpDao = userTpDao;
        this.genericRedisTemplate = genericRedisTemplate;
    }

    @GetMapping
    public void extractDailyMqData(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sein", required = false) String sein,
            @RequestParam(value = "isLatest", required = false) String isLatest,
            @RequestParam(value = "tradingDate", required = false) String tradingDate,
            @RequestParam(value = "billingPeriodStart", required = false) String billingPeriodStart,
            @RequestParam(value = "billingPeriodEnd", required = false) String billingPeriodEnd,
            HttpServletResponse response) throws Exception {

        log.info("/mq-data/extraction accessed by user=[{}]", SecurityUtils.getUsername());

        byte[] result;
        String fileName;

        if (StringUtils.isBlank(category)
                || StringUtils.isBlank(sein)
                || StringUtils.isBlank(isLatest)
                || (DAILY.equalsIgnoreCase(category) && StringUtils.isBlank(tradingDate))
                || (MONTHLY.equalsIgnoreCase(category) && StringUtils.isBlank(billingPeriodStart))
                || (MONTHLY.equalsIgnoreCase(category) && StringUtils.isBlank(billingPeriodEnd))) {
            MissingParametersDto missingParametersDto = new MissingParametersDto(HttpStatus.BAD_REQUEST, "/mq-data/extraction")
                    .addToMissingParams("category", category)
                    .addToMissingParams("sein", sein)
                    .addToMissingParams("isLatest", isLatest);

            if (DAILY.equalsIgnoreCase(category)) {
                missingParametersDto = missingParametersDto.addToMissingParams("tradingDate", tradingDate);
            } else if (MONTHLY.equalsIgnoreCase(category)) {
                missingParametersDto = missingParametersDto.addToMissingParams("billingPeriodStart", billingPeriodStart)
                        .addToMissingParams("billingPeriodEnd", billingPeriodEnd);
            }

            result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(missingParametersDto)
                    .getBytes(Charset.forName("UTF-8"));
            response.setStatus(400);
            fileName = URLDecoder.decode(URLEncoder.encode(String.format("missing_fields_%d", System.currentTimeMillis())
                    , "UTF-8"), "ISO8859_1");
        } else {
            boolean isDaily;
            switch (category.toUpperCase()) {
                case DAILY:
                    isDaily = true;
                    break;
                case MONTHLY:
                    isDaily = false;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid meter data category " + category);
            }

            log.info("userId={}", SecurityUtils.getUserId());

            String tpShortName = getLoggedInUserParticipant();

            switch (isLatest.toUpperCase()) {
                case "ALL":
                case "LATEST":
                    break;
                default:
                    throw new IllegalArgumentException("Invalid version " + isLatest);
            }

            if (isDaily) {
                fileName = URLEncoder.encode(String.format("MQ_%s_%s_%s_%s_%s_%s.json",
                        tpShortName,
                        category.toUpperCase(),
                        isLatest.toUpperCase(),
                        sein,
                        tradingDate.replaceAll("-", ""),
                        DATETIME_FORMAT.format(LocalDateTime.now())
                ), "UTF-8");

                MqExtractionHeader header = processMqReport(category.toUpperCase(), sein, tpShortName, tradingDate,
                        "LATEST".equalsIgnoreCase(isLatest));
                result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(header).getBytes(Charset.forName("UTF-8"));

            } else {
                final ByteArrayOutputStream bao = new ByteArrayOutputStream();
                final ZipOutputStream zOut = new ZipOutputStream(bao);
                String strDate;

                for (LocalDateTime startDate = toLocalDateTime(billingPeriodStart), endDate = toLocalDateTime(billingPeriodEnd);
                     startDate.isBefore(endDate) || startDate.isEqual(endDate); startDate = startDate.plusDays(1L)) {
                    strDate = DATE_FORMAT.format(startDate);

                    String headerStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                            processMqReport(category.toUpperCase(), sein, tpShortName, strDate, "LATEST".equalsIgnoreCase(isLatest)));

                    fileName = URLEncoder.encode(String.format("MQ_%s_%s_%s_%s_%s_%s.json",
                            tpShortName,
                            category.toUpperCase(),
                            isLatest.toUpperCase(),
                            sein,
                            strDate.replaceAll("-", ""),
                            DATETIME_FORMAT.format(LocalDateTime.now())
                    ), "UTF-8");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zOut.putNextEntry(zipEntry);
                    byte[] bytes = headerStr.getBytes();
                    baos.write(bytes, 0, bytes.length);
                    baos.close();
                    zOut.write(baos.toByteArray());
                    zOut.closeEntry();
                }

                zOut.close();
                bao.close();

                result = bao.toByteArray();

                fileName = URLEncoder.encode(String.format("MQ_%s_%s_%s_%s_%s.zip",
                        tpShortName,
                        category.toUpperCase(),
                        isLatest.toUpperCase(),
                        sein,
                        DATETIME_FORMAT.format(LocalDateTime.now())
                ), "UTF-8");
            }

            fileName = URLDecoder.decode(fileName, "ISO8859_1");
        }

        response.setContentType("application/x-msdownload");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.addHeader("Content-disposition", "attachment; filename=" + fileName);

        try (OutputStream os = response.getOutputStream()) {
            os.write(result);
        }

        genericRedisTemplate.convertAndSend(AUDIT_TOPIC_NAME, buildAudit(
                Module.METERING.getDescription(),
                Function.MQ_DATA_EXTRACTION_WEB_SERVICE.getDescription(),
                "Web Service MQ Data Extraction",
                SecurityUtils.getUsername(),
                buildAuditDetails(createKeyValue("Files", fileName)),
                createDetails("Success", "")));
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


    private LocalDateTime toLocalDateTime(String readingDate) {
        LocalDateTime retVal = null;

        if (isNotBlank(readingDate)) {
            retVal = LocalDate.parse(readingDate, DATE_FORMAT).atStartOfDay();
        }

        return retVal;
    }

    private String getLoggedInUserParticipant() {
//        return "NGCP";
        return userTpDao.findBShortNameByTpId(SecurityUtils.getUserId().longValue());
    }
}

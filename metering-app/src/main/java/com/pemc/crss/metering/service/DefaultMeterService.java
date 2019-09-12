package com.pemc.crss.metering.service;

import com.google.common.collect.Lists;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.dto.VersionData;
import com.pemc.crss.metering.dto.mq.*;
import com.pemc.crss.metering.event.MeterQuantityUploadEvent;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityParser;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.mq.MQValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static java.util.Calendar.MINUTE;

@Slf4j
@Service
public class DefaultMeterService implements MeterService {

    private final MeteringDao meteringDao;
    private final MeterQuantityParser meterQuantityParser;
    private final MQValidationHandler validationHandler;
    private final ApplicationEventPublisher eventPublisher;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    private static final int DECIMAL_PLACE_SIZE = 17;

    @Autowired
    public DefaultMeterService(MeteringDao meteringDao, MeterQuantityParser meterQuantityParser, MQValidationHandler validationHandler,
                               ApplicationEventPublisher eventPublisher) {

        this.meteringDao = meteringDao;
        this.meterQuantityParser = meterQuantityParser;
        this.validationHandler = validationHandler;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Long saveHeader(HeaderParam headerParam, String closureTime, String allowableDate) {
        String userName = getUserName();

        HeaderManifest manifest = new HeaderManifest();
        manifest.setTransactionID(UUID.randomUUID().toString());
        manifest.setFileCount(headerParam.getFileCount());
        manifest.setCategory(headerParam.getCategory());
        manifest.setMspShortName(headerParam.getMspShortName());
        manifest.setUploadedBy(userName);
        manifest.setUploadDateTime(new Date());
        manifest.setConvertedToFiveMin(headerParam.getConvertToFiveMin() == null
                ? "N" : headerParam.getConvertToFiveMin() ? "Y" : "N");
        manifest.setAllowableDate(allowableDate);
        manifest.setClosureTime(closureTime);

        return meteringDao.saveHeader(manifest);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHeaderValid(long headerID) {
        return meteringDao.isHeaderValid(headerID);
    }

    @Override
    public String getUserName() {
        String retVal = "";

        if (SecurityContextHolder.getContext() == null) {
            return null;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        if (auth.getPrincipal() != null && auth.getPrincipal() instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> oAuthPrincipal = (LinkedHashMap) auth.getPrincipal();

            retVal = (String) oAuthPrincipal.get("name");
        }

        return retVal;
    }

    @Override
    @Transactional
    public String saveTrailer(long headerID) {
        return meteringDao.saveTrailer(headerID);
    }

    @Override
    public void processMeterData(FileManifest fileManifest, byte[] fileContent) {
        HeaderManifest headerManifest = meteringDao.getHeaderManifest(fileManifest.getHeaderID());

        fileManifest.setTransactionID(headerManifest.getTransactionID());
        fileManifest.setMspShortName(headerManifest.getMspShortName());
        fileManifest.setUploadDateTime(headerManifest.getUploadDateTime());

        UploadType uploadType = UploadType.valueOf(headerManifest.getCategory().toUpperCase());
        fileManifest.setUploadType(uploadType);

        long fileID = saveFileManifest(fileManifest);
        fileManifest.setFileID(fileID);
        log.debug("Saved manifest file fileID:{}", fileID);

        ValidationResult validationResult = new ValidationResult();

        MeterData meterData = new MeterData();
        try {
            meterData = meterQuantityParser.parse(fileManifest, fileContent);
            meterData.setConvertToFiveMin(headerManifest.getConvertedToFiveMin().equals("Y"));
            log.debug("Finished parsing MQ file {} for {} records", fileManifest.getFileName(), meterData.getDetails().size());

            validationResult.setStatus(ACCEPTED);
            validationResult.setErrorDetail("");
        } catch (Exception e) {
            validationResult.setStatus(REJECTED);
            validationResult.setErrorDetail(e.getMessage());

            log.error(e.getMessage(), e);
        }

        if (validationResult.getStatus() == ACCEPTED) {
            validationResult = validationHandler.validate(fileManifest, meterData);
            log.debug("Finished validating MQ data status:{} errorDetail:{}",
                    validationResult.getStatus(), validationResult.getErrorDetail());
        }

        if (validationResult.getStatus() == ACCEPTED) {
            List<MeterDataDetail> meterDataDetails = meterData.getDetails();
            if (headerManifest.getConvertedToFiveMin().equals("Y")) {
                meterDataDetails = convertToFiveMin(meterDataDetails);
            }
            validationResult = saveMeterData(fileManifest, meterDataDetails);
        }

        validationResult.setFileID(fileManifest.getFileID());
        meteringDao.updateManifestStatus(validationResult);

        log.debug("Firing notification headerID:{} fileID:{}", fileManifest.getHeaderID(), fileManifest.getFileID());
        eventPublisher.publishEvent(new MeterQuantityUploadEvent(fileManifest.getHeaderID()));
    }

    @Override
    @Transactional
    public long saveFileManifest(FileManifest fileManifest) {
        return meteringDao.saveFileManifest(fileManifest);
    }

    @Override
    @Transactional
    public ValidationResult saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails) {
        ValidationResult retVal = new ValidationResult();
        retVal.setFileID(fileManifest.getFileID());
        retVal.setStatus(ACCEPTED);

        try {
            meteringDao.saveMeterData(fileManifest, meterDataDetails);
            log.debug("Finished saving MQ data to the database. fileID:{} fileName:{} records:{}",
                    fileManifest.getFileID(), fileManifest.getFileName(), meterDataDetails.size());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);

            retVal.setStatus(REJECTED);
            retVal.setErrorDetail(e.getMessage());
        }

        return retVal;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterDataDisplay> getMeterData(PageableRequest pageableRequest) {
        int totalRecords = meteringDao.getTotalRecords(pageableRequest);

        List<MeterDataDisplay> meterDataList = meteringDao.findAll(pageableRequest);

        return new PageImpl<>(
                meterDataList,
                pageableRequest.getPageable(),
                totalRecords);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFileProcessingCompleted(long headerID) {
        return meteringDao.isFileProcessingCompleted(headerID);
    }

    @Override
    @Transactional(readOnly = true)
    public MeterQuantityReport getReport(long headerId) {
        return meteringDao.getManifestReport(headerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileManifest> getAllFileManifest(long headerID) {
        return meteringDao.getFileManifest(headerID);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnprocessedFileCount(long headerID) {
        return meteringDao.getUnprocessedFileCount(headerID);
    }

    @Override
    @Transactional
    public void updateNotificationFlag(long headerID) {
        meteringDao.updateNotificationFlag(headerID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getStaleRecords() {
        return meteringDao.getStaleRecords();
    }

    @Override
    public List<FileManifest> checkStatus(Long headerID) {
        return meteringDao.getFileManifestStatus(headerID);
    }

    @Override
    public HeaderManifest getHeader(Long headerID) {
        return meteringDao.getHeaderManifest(headerID);
    }

    @Override
    public List<VersionData> getVersionedData(Map<String, String> request) {
        return meteringDao.getVersionedData(request);
    }

    @Override
    public List<ProcessedMqData> getMeterDataForExtraction(String category, String sein, String tpShortName,
                                                           String dateFrom, String dateTo, boolean isLatest) {
        return meteringDao.findAllForExtraction(category, sein, tpShortName, dateFrom, dateTo, isLatest);
    }

    private List<MeterDataDetail> convertToFiveMin(List<MeterDataDetail> meterDataDetails) {
        MeterDataDetail firstRecord = meterDataDetails.get(0);
        MeterDataDetail secondRecord = meterDataDetails.get(1);
        Calendar expectedDateTime = Calendar.getInstance();
        try {
            expectedDateTime.setTime(DATE_FORMAT.parse(String.valueOf(firstRecord.getReadingDateTime())));
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }

        expectedDateTime.add(MINUTE, 5);
        long expected = Long.parseLong(DATE_FORMAT.format(expectedDateTime.getTime()));
        if (secondRecord.getReadingDateTime() == expected) {
            return meterDataDetails;
        }

        List<MeterDataDetail> retVal = new ArrayList<>();
        meterDataDetails.forEach(meterDataDetail -> {
            BigDecimal kwd = divide(meterDataDetail.getKwd(), 3);
            BigDecimal kwhd = divide(meterDataDetail.getKwhd(), 3);
            BigDecimal kvarhd = divide(meterDataDetail.getKvarhd(), 3);
            BigDecimal kwr = divide(meterDataDetail.getKwr(), 3);
            BigDecimal kwhr = divide(meterDataDetail.getKwhr(), 3);
            BigDecimal kvarhr = divide(meterDataDetail.getKvarhr(), 3);
            Calendar newTime = Calendar.getInstance();
            try {
                newTime.setTime(DATE_FORMAT.parse(String.valueOf(meterDataDetail.getReadingDateTime())));
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
            List<Integer> intervalDifferences = Lists.newArrayList(0, -5, -5);
            intervalDifferences.forEach(diff -> {
                newTime.add(MINUTE, diff);
                MeterDataDetail newMeterDataDetail = new MeterDataDetail(meterDataDetail);
                newMeterDataDetail.setKwd(kwd);
                newMeterDataDetail.setKwhd(kwhd);
                newMeterDataDetail.setKvarhd(kvarhd);
                newMeterDataDetail.setKwr(kwr);
                newMeterDataDetail.setKwhr(kwhr);
                newMeterDataDetail.setKvarhr(kvarhr);
                newMeterDataDetail.setReadingDateTime(Long.parseLong(DATE_FORMAT.format(newTime.getTime())));
                retVal.add(newMeterDataDetail);
            });
        });
        return retVal;
    }

    private BigDecimal divide(BigDecimal dividend, int divisor) {
        if (dividend == null) {
            return BigDecimal.ZERO;
        }

        return dividend.divide(new BigDecimal(divisor), DECIMAL_PLACE_SIZE, RoundingMode.HALF_UP);
    }
}

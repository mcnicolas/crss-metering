package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Slf4j
@Service
public class DefaultMeterService implements MeterService {

    private final MeteringDao meteringDao;
    private final MeterQuantityParser meterQuantityParser;
    private final MQValidationHandler validationHandler;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public DefaultMeterService(MeteringDao meteringDao, MeterQuantityParser meterQuantityParser,
                               MQValidationHandler validationHandler, ApplicationEventPublisher eventPublisher) {

        this.meteringDao = meteringDao;
        this.meterQuantityParser = meterQuantityParser;
        this.validationHandler = validationHandler;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public long saveHeader(int fileCount, String category) {
        String userName = getUserName();

        HeaderManifest manifest = new HeaderManifest();
        manifest.setTransactionID(UUID.randomUUID().toString());
        manifest.setFileCount(fileCount);
        manifest.setCategory(category);
        manifest.setUploadedBy(userName);
        manifest.setUploadDateTime(new Date());

        return meteringDao.saveHeader(manifest);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHeaderValid(long headerID) {
        return meteringDao.isHeaderValid(headerID);
    }

    private String getUserName() {
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

        UploadType uploadType = UploadType.valueOf(headerManifest.getCategory().toUpperCase());
        fileManifest.setUploadType(uploadType);

        long fileID = saveFileManifest(fileManifest);
        fileManifest.setFileID(fileID);
        log.debug("Saved manifest file fileID:{}", fileID);

        ValidationResult validationResult = new ValidationResult();

        MeterData meterData = new MeterData();
        try {
            meterData = meterQuantityParser.parse(fileManifest, fileContent);
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
            validationResult = saveMeterData(fileManifest, meterData.getDetails());
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
    public List<FileManifest> findRejectedFiles(long headerId) {
        return meteringDao.findByHeaderAndStatus(headerId, "REJECTED");
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

}

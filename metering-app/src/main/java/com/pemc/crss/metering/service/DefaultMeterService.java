package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.*;
import com.pemc.crss.metering.event.MeterUploadEvent;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityParser;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.mq.MQValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

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
    }

    private void sendNotification(long headerID) {
        HeaderManifest header = meteringDao.getHeaderManifest(headerID);
        List<FileManifest> fileList = meteringDao.getFileManifest(headerID);

        if (isTransactionComplete(header, fileList)) {
            Map<String, Object> messagePayload = new HashMap<>();

            messagePayload.put("uploadedFiles", fileList);
            eventPublisher.publishEvent(new MeterUploadEvent(messagePayload));
        }
    }

    private boolean isTransactionComplete(HeaderManifest header, List<FileManifest> fileList) {
        for (FileManifest fileManifest : fileList) {
            if (!equalsIgnoreCase(fileManifest.getProcessFlag(), "Y")) {
                return false;
            }
        }

        return header.getFileCount() == fileList.size();
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
    public boolean isFileProcessingCompleted(long headerId) {
        return meteringDao.isFileProcessingCompleted(headerId);
    }

    @Override
    @Transactional(readOnly = true)
    public MeterQuantityReport getReport(long headerId) {
        MeterQuantityReport report = meteringDao.getManifestReport(headerId);
        Validate.notNull(report, "Cannot found Meter quantity report with manifest header id " + headerId);

        return report;
    }

    @Override
    public List<FileManifest> findRejectedFiles(long headerId) {
        return meteringDao.findByHeaderAndStatus(headerId, "REJECTED");
    }
}

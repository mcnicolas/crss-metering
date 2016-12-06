package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.event.MeterUploadEvent;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityParser;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.mq.MQValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public long saveHeader(String transactionID, int fileCount, String category, String username) {
        return meteringDao.saveHeader(transactionID, fileCount, category, username);
    }

    @Override
    @Transactional
    public void saveTrailer(String transactionID) {
        meteringDao.saveTrailer(transactionID);
    }

    @Override
    public void processMeterData(FileManifest fileManifest, byte[] fileContent) {
        // NOTE:
        // 2. Parse data
        // 3. Validate data
        // 4. Save data
        // 5. check for completed records
        // 6. Send email

        long fileID = saveFileManifest(fileManifest);
        fileManifest.setFileID(fileID);
        log.debug("Saved manifest file fileID:{}", fileID);

        // Parse meter data
        ValidationResult validationResult = new ValidationResult();

        MeterData meterData = new MeterData();
        try {
            meterData = meterQuantityParser.parse(fileManifest.getFileType(), fileContent);
            log.debug("Finished parsing MQ data for {} records", meterData.getDetails().size());

            validationResult.setStatus(ACCEPTED);
            validationResult.setErrorDetail("");
        } catch (ParseException e) {
            validationResult.setStatus(REJECTED);
            validationResult.setErrorDetail(e.getMessage());

            log.error(e.getMessage(), e);
        }

        // Validate meter data
        if (validationResult.getStatus() == ACCEPTED) {
            validationResult = validationHandler.validate(fileManifest, meterData);
            log.debug("Finished validating MQ data accept_reject:{} status:{}",
                    validationResult.getStatus(), validationResult.getErrorDetail());

            // Save meter data
            saveMeterData(fileManifest, meterData.getDetails());
        }

        // Update manifest
        validationResult.setFileID(fileManifest.getFileID());
        meteringDao.updateManifestStatus(validationResult);

        sendNotification(fileManifest.getTransactionID());
    }

    private void sendNotification(String transactionID) {
        HeaderManifest header = meteringDao.getHeaderManifest(transactionID);
        List<FileManifest> fileList = meteringDao.getFileManifest(transactionID);

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
    public void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails) {
        meteringDao.saveMeterData(fileManifest, meterDataDetails);
        log.debug("Finished saving MQ data to the database. fileID:{} records:{}", fileManifest.getFileID(), meterDataDetails.size());
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

}

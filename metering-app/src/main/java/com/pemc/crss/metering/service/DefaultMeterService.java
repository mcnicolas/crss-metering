package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityParser;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.mq.MQValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Slf4j
@Service
public class DefaultMeterService implements MeterService {

    private final MeteringDao meteringDao;
    private final MeterQuantityParser meterQuantityParser;
    private final MQValidationHandler validationHandler;

    @Autowired
    public DefaultMeterService(MeteringDao meteringDao, MeterQuantityParser meterQuantityParser, MQValidationHandler validationHandler) {
        this.meteringDao = meteringDao;
        this.meterQuantityParser = meterQuantityParser;
        this.validationHandler = validationHandler;
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
            log.debug("Finished parsing MQ data for {} records", meterData.getMeterDataDetails().size());

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
            saveMeterData(fileManifest, meterData.getMeterDataDetails());
        }

        // Update manifest
        validationResult.setFileID(fileManifest.getFileID());
        meteringDao.updateManifestStatus(validationResult);

        // TODO: Check for completed records
        // 1. Completed records should be sent via email detailing the files with accept/reject status
        // 2. Find a way to make this efficient
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

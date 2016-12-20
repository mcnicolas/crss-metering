package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.validator.ValidationResult;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MeterService {

    long saveHeader(int fileCount, String category);

    String saveTrailer(long transactionID);

    long saveFileManifest(FileManifest fileManifest);

    Page<MeterDataDisplay> getMeterData(PageableRequest pageableRequest);

    void processMeterData(FileManifest fileManifest, byte[] fileContent);

    ValidationResult saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails);

    boolean isFileProcessingCompleted(long headerId);

    MeterQuantityReport getReport(long headerId);

    List<FileManifest> findRejectedFiles(long headerId);
}

package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.dto.VersionData;
import com.pemc.crss.metering.dto.mq.*;
import com.pemc.crss.metering.validator.ValidationResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface MeterService {

    Long saveHeader(HeaderParam headerParam);

    boolean isHeaderValid(long headerID);

    String getUserName();

    String saveTrailer(long transactionID);

    long saveFileManifest(FileManifest fileManifest);

    Page<MeterDataDisplay> getMeterData(PageableRequest pageableRequest);

    void processMeterData(FileManifest fileManifest, byte[] fileContent);

    ValidationResult saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails);

    boolean isFileProcessingCompleted(long headerId);

    MeterQuantityReport getReport(long headerId);

    List<FileManifest> getAllFileManifest(long headerID);

    Integer getUnprocessedFileCount(long headerID);

    void updateNotificationFlag(long headerID);

    List<Long> getStaleRecords();

    List<FileManifest> checkStatus(Long headerID);

    HeaderManifest getHeader(Long headerID);

    List<VersionData> getVersionedData(Map<String, String> request);

    List<ProcessedMqData> getMeterDataForExtraction(String category, String sein, String tpShortName,
                                                    String dateFrom, String dateTo, boolean isLatest);

//    ParticipantUserDetail getParticipantUserDetail(String shortName);

}

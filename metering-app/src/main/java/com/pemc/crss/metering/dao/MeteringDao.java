package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.dto.VersionData;
import com.pemc.crss.metering.validator.ValidationResult;

import java.util.List;
import java.util.Map;

public interface MeteringDao {

    long saveHeader(HeaderManifest manifest);

    boolean isHeaderValid(long headerID);

    String saveTrailer(long headerID);

    long saveFileManifest(FileManifest fileManifest);

    List<MeterDataDisplay> findAll(PageableRequest pageableRequest);

    List<VersionData> getVersionedData(Map<String, String> param);

    int getTotalRecords(PageableRequest pageableRequest);

    void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails);

    void updateManifestStatus(ValidationResult validationResult);

    HeaderManifest getHeaderManifest(long headerId);

    List<FileManifest> getFileManifest(long headerID);

    int getUnprocessedFileCount(long headerID);

    List<FileManifest> getFileManifestStatus(long headerID);

    boolean isFileProcessingCompleted(long headerId);

    MeterQuantityReport getManifestReport(long headerId);

    List<FileManifest> findByHeaderAndStatus(long headerId, String status);

    void updateNotificationFlag(long headerID);

    List<Long> getStaleRecords();

    List<ProcessedMqData> findAllForExtraction(String category, String sein, String tpShortName,
                                               String dateFrom, String dateTo, boolean isLatest);

}

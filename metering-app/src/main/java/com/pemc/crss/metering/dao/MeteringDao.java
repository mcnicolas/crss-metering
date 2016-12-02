package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;

import java.util.List;

public interface MeteringDao {

    long saveHeader(String transactionID, int fileCount, String category, String username);

    void saveTrailer(String transactionID);

    long saveFileManifest(FileManifest fileManifest);

    List<MeterDataDisplay> findAll(PageableRequest pageableRequest);

    int getTotalRecords(PageableRequest pageableRequest);

    void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails);

    void updateManifestStatus(ValidationResult validationResult);
}

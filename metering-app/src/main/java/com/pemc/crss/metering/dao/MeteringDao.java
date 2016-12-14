package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;

import java.util.List;

public interface MeteringDao {

    long saveHeader(HeaderManifest manifest);

    String saveTrailer(long headerID);

    long saveFileManifest(FileManifest fileManifest);

    List<MeterDataDisplay> findAll(PageableRequest pageableRequest);

    int getTotalRecords(PageableRequest pageableRequest);

    void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails);

    void updateManifestStatus(ValidationResult validationResult);

    HeaderManifest getHeaderManifest(long transactionID);

    List<FileManifest> getFileManifest(long headerID);

}

package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MeterService {

    long saveHeader(String transactionID, int fileCount, String category, String username);

    void saveTrailer(String transactionID);

    long saveFileManifest(FileManifest fileManifest);

    Page<MeterDataDisplay> getMeterData(PageableRequest pageableRequest);

    void processMeterData(FileManifest fileManifest, byte[] fileContent);

    void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails);

}

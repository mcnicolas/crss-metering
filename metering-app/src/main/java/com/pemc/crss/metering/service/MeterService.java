package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

public interface MeterService {

    long saveHeader(String transactionID, long mspID, int fileCount, String category, String username);

    void saveTrailer(String transactionID);

    long saveFileManifest(long headerID, String transactionID, String fileName, String fileType, long fileSize, String checksum);

    void saveMeterData(long fileID, String fileType, byte[] fileContent, String category);

    Page<MeterDataDisplay> getMeterData(PageableRequest pageableRequest);

    void saveMeterData(Collection<MultipartFile> values, UploadType uploadType) throws IOException;

}

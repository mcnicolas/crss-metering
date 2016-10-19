package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.UploadType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

public interface MeterService {

    long saveHeader(String transactionID, long mspID, int fileCount, String category, String username);

    void saveTrailer(String transactionID);

    void saveFileManifest(long headerID, String transactionID, String fileName, String fileType, long fileSize, String checksum);

    void saveMeterData(Collection<MultipartFile> values, UploadType uploadType) throws IOException;

}

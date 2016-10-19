package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterDataXLS;
import com.pemc.crss.metering.dto.MeterUploadFile;
import com.pemc.crss.metering.dto.MeterUploadHeader;

import java.util.List;

public interface MeteringDao {

    long saveHeader(String transactionID, long mspID, int fileCount, String category, String username);

    void saveTrailer(String transactionID);

    void saveFileManifest(long headerID, String transactionID, String fileName, String fileType, long fileSize,
                          String checksum);

    long saveMeterUploadHeader(MeterUploadHeader meterUploadHeader);

    long saveMeterUploadFile(long transactionID, MeterUploadFile meterUploadFile);

    void saveMeterUploadMDEF(long fileID, MeterData meterData);

    void saveMeterUploadXLS(long transactionID, List<MeterDataXLS> meterDataList);

}

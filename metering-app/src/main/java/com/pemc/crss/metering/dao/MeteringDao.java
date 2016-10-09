package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterDataXLS;
import com.pemc.crss.metering.dto.MeterUploadMDEF;
import com.pemc.crss.metering.dto.MeterUploadHeader;

import java.util.List;

public interface MeteringDao {

    long saveMeterUploadHeader(MeterUploadHeader meterUploadHeader);

    long saveMeterUploadFile(long transactionID, MeterUploadMDEF meterUploadMDEF);

    void saveMeterUploadMDEF(long fileID, MeterData meterData);

    void saveMeterUploadXLS(long transactionID, List<MeterDataXLS> meterDataList);
}

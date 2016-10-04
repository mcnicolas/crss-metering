package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterUploadMDEF;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.dto.MeterUploadXLS;

public interface MeteringDao {

    long saveMeterUploadHeader(MeterUploadHeader meterUploadHeader);

    long saveMeterUploadFile(long transactionID, MeterUploadMDEF meterUploadMDEF);

    void saveMeterUploadMDEF(long fileID, MeterData meterData);

    void saveMeterUploadXLS(long transactionID, MeterUploadXLS meterUploadXLS);
}

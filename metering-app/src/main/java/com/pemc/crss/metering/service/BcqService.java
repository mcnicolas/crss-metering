package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.List;
import java.util.Map;

public interface BcqService {

    long saveBcqUploadFile(String transactionID, BcqUploadFile bcqUploadFile);

    void saveBcqData(long fileID, Map<BcqHeader, List<BcqData>> headerDataMap);

}

package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.Map;
import java.util.Set;

public interface BcqDao {

    long saveBcqUploadFile(String transactionID, BcqUploadFile uploadFile);

    void saveBcqData(long fileID, Map<BcqHeader, Set<BcqData>> headerDataMap);

}

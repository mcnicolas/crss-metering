package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.dto.BCQUploadFile;

import java.util.List;

public interface BCQDao {

    long saveBCQUploadFile(String transactionID, BCQUploadFile bcqUploadFile);

    void saveBCQData(long fileID, List<BCQData> bcqDataList);

}

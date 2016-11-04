package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.dto.BCQUploadFile;

public interface BCQDao {

    long saveBCQUploadFile(long transactionID, BCQUploadFile bcqUploadFile);

    long saveBCQData(long fileID, BCQData bcqData);

}

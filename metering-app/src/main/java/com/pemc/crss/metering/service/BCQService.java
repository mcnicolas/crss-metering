package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.dto.BCQUploadFile;

import java.util.List;

public interface BCQService {

    long saveBCQUploadFile(String transactionID, BCQUploadFile bcqUploadFile);

    void saveBCQData(long fileID, List<BCQData> dataList);

}

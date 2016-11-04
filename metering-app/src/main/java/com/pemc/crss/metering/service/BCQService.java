package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.BCQUploadFile;

import java.io.IOException;

public interface BCQService {

    long saveBCQUploadFile(String transactionID, BCQUploadFile bcqUploadFile);

    void saveBCQData(long fileID, byte[] fileContent) throws IOException;

}

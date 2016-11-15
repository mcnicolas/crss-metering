package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.BcqHeaderDataPair;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.List;

public interface BcqService {

    void saveBcqDetails(BcqUploadFile file, List<BcqHeaderDataPair> headerDataPairList,
                        List<Long> buyerIds, Long sellerId);

    long saveBcqUploadFile(String transactionID, BcqUploadFile bcqUploadFile);

    void saveBcqData(long fileID, List<BcqHeaderDataPair> headerDataPairList);

}

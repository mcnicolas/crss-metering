package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BcqDeclaration;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.List;

public interface BcqDao {

    long saveBcqUploadFile(String transactionID, BcqUploadFile uploadFile);

    void saveBcqData(long fileID, List<BcqDeclaration> bcqDeclarationList);

}

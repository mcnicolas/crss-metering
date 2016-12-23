package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;

import java.util.Date;
import java.util.List;

public interface BcqService2 {

    long saveUploadFile(BcqUploadFile uploadFile);

    void saveFailedUploadFile(BcqUploadFile uploadFile, BcqDeclaration declaration);

    void saveDeclaration(BcqDeclaration declaration);

    List<BcqHeader> findAllHeadersBySellerAndTradingDate(String sellerShortName, Date tradingDate);

    boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList);

}

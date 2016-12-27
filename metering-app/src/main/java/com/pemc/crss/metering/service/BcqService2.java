package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;

public interface BcqService2 {

    long saveUploadFile(BcqUploadFile uploadFile);

    void saveFailedUploadFile(BcqUploadFile uploadFile, BcqDeclaration declaration);

    void saveDeclaration(BcqDeclaration declaration);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeadersBySellerAndTradingDate(String sellerShortName, Date tradingDate);

    boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

}

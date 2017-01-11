package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.*;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;

public interface BcqService {

    void saveSellerDeclaration(BcqDeclaration declaration);

    void saveSettlementDeclaration(BcqDeclaration declaration);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeadersBySellerAndTradingDate(String sellerShortName, Date tradingDate);

    List<ParticipantSellerDetails> findAllSellersWithExpiredBcqByTradingDate(Date tradingDate);

    boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

    void updateHeaderStatusBySettlement(long headerId, BcqStatus status);

    void approve(long headerId);

    void processUnconfirmedHeaders();

    void processUnnullifiedHeaders();

}

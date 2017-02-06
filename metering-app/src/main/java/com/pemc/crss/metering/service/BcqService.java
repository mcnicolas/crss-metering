package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqHeaderDisplay2;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BcqService {

    void saveSellerDeclaration(BcqDeclaration declaration);

    void saveSettlementDeclaration(BcqDeclaration declaration);

    Page<BcqHeaderDisplay2> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeaders(Map<String, String> mapParams);

    boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

    void updateHeaderStatusBySettlement(long headerId, BcqStatus status);

    void approve(long headerId);

    void processUnconfirmedHeaders();

    void processUnnullifiedHeaders();

    List<BcqSpecialEventList> getSpecialEvents();

    long saveSpecialEvent(BcqSpecialEvent specialEvent);

}

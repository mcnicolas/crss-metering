package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqEventValidationData;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BcqDao {

    long saveUploadFile(BcqUploadFile uploadFile);

    List<BcqHeader> saveHeaderList(List<BcqHeader> headerList);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeaders(Map<String, String> params);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

    void updateHeaderStatusBySettlement(long headerId, BcqStatus status);

    List<BcqSpecialEventList> getAllSpecialEvents();

    long saveSpecialEvent(BcqSpecialEvent specialEvent);

    List<BcqEventValidationData> checkDuplicateParticipantTradingDates(List<String> tradingParticipants,
                                                                       List<Date> tradingDates);

}

package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.reports.ReportBean;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.query.ComparisonOperator;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqEventValidationData;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BcqDao {

    long saveUploadFile(BcqUploadFile uploadFile);

    List<BcqHeader> saveHeaders(List<BcqHeader> headerList, boolean isSpecialEvent);

    Page<BcqHeaderPageDisplay> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeaders(Map<String, String> params);

    List<BcqHeader> findSameHeaders(BcqHeader header, List<BcqStatus> statuses, ComparisonOperator operator);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

    void updateHeaderStatusBySettlement(long headerId, BcqStatus status);

    List<BcqSpecialEventList> findAllSpecialEvents();

    long saveSpecialEvent(BcqSpecialEvent specialEvent);

    List<BcqEventValidationData> checkDuplicateParticipantTradingDates(List<String> tradingParticipants,
                                                                       List<Date> tradingDates);

    List<BcqSpecialEventParticipant> findEventParticipantsByTradingDate(Date tradingDate);

    Date findEventDeadlineDateByTradingDateAndParticipant(Date tradingDate, String shortName);

    List<ReportBean> queryBcqDataReport(Map<String, String> mapParams);

    List<Long> selectByStatusAndDeadlineDatePlusDays(BcqStatus status, Integer plusDays);

    Page<BcqProhibitedPairPageDisplay> findAllProhibited(PageableRequest pageableRequest);

}

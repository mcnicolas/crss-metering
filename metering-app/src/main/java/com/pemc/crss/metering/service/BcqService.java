package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.query.ComparisonOperator;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BcqService {

    void saveDeclaration(BcqDeclaration declaration, boolean isSettlement);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    Page<BcqHeaderPageDisplay> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeaders(Map<String, String> mapParams);

    List<BcqHeader> findSameHeaders(BcqHeader header, List<BcqStatus> statuses, ComparisonOperator operator);

    List<BcqHeader> findHeadersOfParticipantByTradingDate(String shortName, Date tradingDate);

    boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList);

    void updateHeaderStatus(long headerId, BcqStatus status);

    void requestForCancellation(long headerId);

    void approve(long headerId);

    void processUnconfirmedHeaders();

    void processUnnullifiedHeaders();

    void processHeadersToSettlementReady();

    List<BcqSpecialEventList> findAllSpecialEvents();

    long saveSpecialEvent(BcqSpecialEvent specialEvent);

    List<BcqSpecialEventParticipant> findEventParticipantsByTradingDate(Date tradingDate);

    Date findEventDeadlineDateByTradingDateAndParticipant(Date tradingDate, String shortName);

    Page<BcqProhibitedPairPageDisplay> findAllProhibitedPairs(PageableRequest pageableRequest);

    long saveProhibitedPair(BcqProhibitedPair prohibitedPair);

    void disableProhibitedPair(long id);

    List<BcqProhibitedPair> findAllEnabledProhibitedPairs();

    void generateCsv(BcqDownloadDto bcqDownloadDto, Long interval, LocalDateTime date, OutputStream outputStream, String currentUser)
            throws IOException;

    void generateInternalCsv(String shortName, Long interval, LocalDateTime date, HttpServletResponse response)
            throws IOException;


    void generateJsonBcqSubmission(String shortName, Date tradingDate, String status, HttpServletResponse response)
            throws IOException;

    void generateSuccessAuditLog(BcqDeclaration bcqDeclaration);

    void generateErrorAuditLog(BcqDeclaration bcqDeclaration, String tradingDate, String shortName );

    String findTradingDate(MultipartFile file);
}

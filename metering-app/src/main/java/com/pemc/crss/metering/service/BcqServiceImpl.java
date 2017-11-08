package com.pemc.crss.metering.service;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dao.query.ComparisonOperator;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqEventValidationData;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant;
import com.pemc.crss.metering.service.exception.InvalidStateException;
import com.pemc.crss.metering.service.exception.OldRecordException;
import com.pemc.crss.metering.service.exception.PairExistsException;
import com.pemc.crss.metering.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.BcqUpdateType.MANUAL_OVERRIDE;
import static com.pemc.crss.metering.constants.BcqUpdateType.RESUBMISSION;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.IN;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.NOT_IN;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqServiceImpl implements BcqService {

    private static final String UNABLE_MESSAGE = "Unable to proceed, ";
    private static final String RELOAD_MESSAGE = "Please reload the page.";
    private static final String NEW_VERSION_ERROR = UNABLE_MESSAGE + "declaration has a new version. " + RELOAD_MESSAGE;
    private static final String VOIDED_ERROR = UNABLE_MESSAGE + "declaration has been voided. " + RELOAD_MESSAGE;
    private static final String UPDATED_ERROR = UNABLE_MESSAGE + "declaration has been updated. " + RELOAD_MESSAGE;

    private final BcqDao bcqDao;
    private final BcqNotificationManager bcqNotificationManager;
    private final CacheConfigService configService;

    @Override
    @Transactional
    public void saveDeclaration(BcqDeclaration declaration, boolean isSettlement) {
        BcqUploadFile uploadFile = declaration.getUploadFileDetails().target();
        uploadFile.setFileId(saveUploadFile(uploadFile));
        if (uploadFile.getValidationStatus() == REJECTED) {
            if (isSettlement) {
                bcqNotificationManager.sendSettlementValidationNotification(declaration, uploadFile.getSubmittedDate());
            } else {
                bcqNotificationManager.sendValidationNotification(declaration, uploadFile.getSubmittedDate());
            }
            return;
        }
        List<BcqHeader> headerList = extractHeaderList(declaration);
        headerList = setUploadFileOfHeaders(headerList, uploadFile);
        if (isSettlement) {
            headerList = setUpdatedViaOfHeadersBySettlement(headerList, declaration);
        } else {
            headerList = setUpdatedViaOfHeaders(headerList, declaration);
        }
        List<BcqHeader> savedHeaderList = bcqDao.saveHeaders(headerList, !isSettlement && declaration.isSpecialEvent());
        if (isSettlement) {
            bcqNotificationManager.sendSettlementUploadNotification(savedHeaderList);
        } else {
            bcqNotificationManager.sendUploadNotification(savedHeaderList);
        }
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        return bcqDao.findHeader(headerId);
    }

    @Override
    public Page<BcqHeaderPageDisplay> findAllHeaders(PageableRequest pageableRequest) {
        return bcqDao.findAllHeaders(pageableRequest);
    }

    @Override
    public List<BcqHeader> findAllHeaders(Map<String, String> mapParams) {
        return bcqDao.findAllHeaders(mapParams);
    }

    @Override
    public List<BcqHeader> findSameHeaders(BcqHeader header, List<BcqStatus> statuses, ComparisonOperator operator) {
        return bcqDao.findSameHeaders(header, statuses, operator);
    }

    @Override
    public List<BcqHeader> findHeadersOfParticipantByTradingDate(String shortName, Date tradingDate) {
        return findAllHeaders(of(
                "sellingParticipant", shortName,
                "tradingDate", formatDate(tradingDate)));
    }

    @Override
    public boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList) {
        return headerList.stream().anyMatch(header -> isSameHeader(header, headerToFind));
    }

    @Override
    public List<BcqData> findDataByHeaderId(long headerId) {
        return bcqDao.findDataByHeaderId(headerId);
    }

    @Override
    @Transactional
    public void updateHeaderStatus(long headerId, BcqStatus newStatus) {
        BcqHeader header = findHeader(headerId);
        validateIfLatest(header);
        validateUpdateStatus(header.getStatus(), newStatus);

        if (newStatus == CONFIRMED) {
            updatePreviousHeaderStatus(header, CONFIRMED);
        }

        bcqDao.updateHeaderStatus(headerId, newStatus);
        bcqNotificationManager.sendUpdateStatusNotification(findHeader(headerId));
    }

    @Override
    @Transactional
    public void requestForCancellation(long headerId) {
        BcqHeader header = findHeader(headerId);
        validateIfLatest(header);
        validateRequestForCancellation(header.getStatus());

        header.setStatus(FOR_APPROVAL_CANCEL);
        bcqDao.updateHeaderStatusBySettlement(headerId, FOR_APPROVAL_CANCEL);
        bcqNotificationManager.sendSettlementUpdateStatusNotification(header);
    }

    @Override
    @Transactional
    public void approve(long headerId) {
        BcqHeader header = findHeader(headerId);
        validateIfLatest(header);
        validateApproval(header.getStatus());

        BcqStatus newStatus;
        if (header.getStatus() == FOR_APPROVAL_CANCEL) {
            newStatus = CANCELLED;
        } else {
            newStatus = SETTLEMENT_READY;
        }

        if (newStatus == SETTLEMENT_READY) {
            updatePreviousHeaderStatus(header, SETTLEMENT_READY);
        }

        bcqDao.updateHeaderStatus(headerId, newStatus);
        bcqNotificationManager.sendApprovalNotification(header);
    }

    @Override
    @Transactional
    public void processUnconfirmedHeaders() {
        List<BcqHeader> unconfirmedHeaders = getExpiredHeadersByStatus(FOR_CONFIRMATION);
        unconfirmedHeaders.forEach(header -> bcqDao.updateHeaderStatus(header.getHeaderId(), NOT_CONFIRMED));
        Map<Map<String, Object>, List<BcqHeader>> groupedHeaders = getGroupedHeaderList(unconfirmedHeaders);
        groupedHeaders.forEach((map, headerList) -> bcqNotificationManager
                .sendUnprocessedNotification(headerList, NOT_CONFIRMED));
    }

    @Override
    @Transactional
    public void processUnnullifiedHeaders() {
        List<BcqHeader> unnullifiedHeaders = getExpiredHeadersByStatus(FOR_NULLIFICATION);
        unnullifiedHeaders.forEach(header -> {
            updatePreviousHeaderStatus(header, CONFIRMED);
            bcqDao.updateHeaderStatus(header.getHeaderId(), CONFIRMED);
        });
        Map<Map<String, Object>, List<BcqHeader>> groupedHeaders = getGroupedHeaderList(unnullifiedHeaders);
        groupedHeaders.forEach((map, headerList) -> bcqNotificationManager
                .sendUnprocessedNotification(headerList, CONFIRMED));
    }

    @Override
    @Transactional
    public void processHeadersToSettlementReady() {
        int plusDays = configService.getIntegerValueForKey("BCQ_SETTLEMENT_READY_DEADLINE_PLUS_DAYS", 2);
        List<Long> headerIdsToUpdate = bcqDao.selectByStatusAndDeadlineDatePlusDays(CONFIRMED, plusDays);
        log.info("[BCQ Service] Found the following header ids to be updated to {}: {}", SETTLEMENT_READY, headerIdsToUpdate);
        headerIdsToUpdate.forEach(id -> bcqDao.updateHeaderStatus(id, SETTLEMENT_READY));
    }

    @Override
    public List<BcqSpecialEventList> findAllSpecialEvents() {
        return bcqDao.findAllSpecialEvents();
    }

    @Override
    @Transactional
    public long saveSpecialEvent(BcqSpecialEvent specialEvent) {
        List<String> tradingParticipantStr = specialEvent.getTradingParticipants().stream()
                .map(BcqSpecialEventParticipant::getShortName).collect(Collectors.toList());

        List<BcqEventValidationData> result = bcqDao.checkDuplicateParticipantTradingDates(
                tradingParticipantStr, specialEvent.getTradingDates());

        if (!result.isEmpty()) {
            String uniqueParticipants = result.stream().map(BcqEventValidationData::getTradingParticipant)
                    .distinct().collect(Collectors.joining(", "));

            throw new RuntimeException("The following participants have duplicate trading dates from another special event: <b>"
                    + uniqueParticipants + "</b>");
        }

        return bcqDao.saveSpecialEvent(specialEvent);
    }

    @Override
    public List<BcqSpecialEventParticipant> findEventParticipantsByTradingDate(Date tradingDate) {
        return bcqDao.findEventParticipantsByTradingDate(tradingDate);
    }

    @Override
    public Date findEventDeadlineDateByTradingDateAndParticipant(Date tradingDate, String shortName) {
        return bcqDao.findEventDeadlineDateByTradingDateAndParticipant(tradingDate, shortName);
    }

    @Override
    public Page<BcqProhibitedPairPageDisplay> findAllProhibitedPairs(PageableRequest pageableRequest) {
        return bcqDao.findAllProhibitedPairs(pageableRequest);
    }

    @Override
    @Transactional
    public long saveProhibitedPair(BcqProhibitedPair prohibitedPair) {
        List<BcqProhibitedPair> bcqProhibitedConstains =
                bcqDao.findAllEnabledProhibitedPairs()
                        .stream().filter(prohibited -> prohibited.getSellingMtn().equalsIgnoreCase(prohibitedPair.getSellingMtn())
                        && prohibited.getBillingId().equalsIgnoreCase(prohibitedPair.getBillingId())).collect(toList());
        log.debug("Found {} duplicate prohibited", bcqProhibitedConstains.size());
        if (CollectionUtils.isNotEmpty(bcqProhibitedConstains)) {
            if (prohibitedPair.getEffectiveStartDate() != null) {
                validateOverLapping(prohibitedPair, bcqProhibitedConstains);
            } else {
                String errorMEssage = String.format("Pair <b>%s</b> - <b>%s</b> already exists.",
                        prohibitedPair.getSellingMtn(), prohibitedPair.getBillingId());
                throw new PairExistsException(errorMEssage);
            }


        }
        return bcqDao.saveProhibitedPair(prohibitedPair);
    }

    @Override
    @Transactional
    public void disableProhibitedPair(long id) {
        bcqDao.disableProhibitedPair(id);
    }

    @Override
    public List<BcqProhibitedPair> findAllEnabledProhibitedPairs() {
        return bcqDao.findAllEnabledProhibitedPairs();
    }

    private long saveUploadFile(BcqUploadFile uploadFile) {
        uploadFile.setSubmittedDate(new Date());
        uploadFile.setTransactionId(randomUUID().toString());
        return bcqDao.saveUploadFile(uploadFile);
    }

    private List<BcqHeader> extractHeaderList(BcqDeclaration declaration) {
        return declaration.getHeaderDetailsList().stream().map(headerDetails -> {
            ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
            BcqHeader header = headerDetails.target();
            header.setSellingParticipantName(sellerDetails.getName());
            header.setSellingParticipantShortName(sellerDetails.getShortName());
            return header;
        }).collect(toList());
    }

    private List<BcqHeader> setUploadFileOfHeaders(List<BcqHeader> headerList, BcqUploadFile uploadFile) {
        return headerList.stream().map(header -> {
            header.setUploadFile(uploadFile);
            header.setFileId(uploadFile.getFileId());
            return header;
        }).collect(toList());
    }

    private List<BcqHeader> setUpdatedViaOfHeaders(List<BcqHeader> headerList, BcqDeclaration declaration) {
        if (declaration.isResubmission()) {
            List<BcqHeader> currentHeaderList = findAllHeaders(of(
                    "sellingParticipant", declaration.getSellerDetails().getShortName(),
                    "tradingDate", formatDate(declaration.getHeaderDetailsList().get(0).getTradingDate())
            ));
            return headerList.stream().map(header -> {
                boolean exists = isHeaderInList(header, currentHeaderList);
                header.setExists(exists);
                if (exists) {
                    header.setUpdatedVia(RESUBMISSION);
                }
                return header;
            }).collect(toList());
        }
        return headerList;
    }

    private List<BcqHeader> setUpdatedViaOfHeadersBySettlement(List<BcqHeader> headerList, BcqDeclaration declaration) {
        List<BcqHeader> currentHeaderList = findAllHeaders(of(
                "sellingParticipant", declaration.getSellerDetails().getShortName(),
                "tradingDate", formatDate(declaration.getHeaderDetailsList().get(0).getTradingDate())
        ));
        return headerList.stream().map(header -> {
            boolean exists = isHeaderInList(header, currentHeaderList);
            if (exists) {
                BcqHeader headerInList = findHeaderInList(header, currentHeaderList);
                if (headerInList.getStatus() == FOR_APPROVAL_NEW) {
                    header.setStatus(FOR_APPROVAL_NEW);
                } else {
                    header.setStatus(FOR_APPROVAL_UPDATE);
                }
                header.setExists(true);
            } else {
                header.setStatus(FOR_APPROVAL_NEW);
            }
            header.setUpdatedVia(MANUAL_OVERRIDE);
            return header;
        }).collect(toList());
    }

    private boolean isSameHeader(BcqHeader header1, BcqHeader header2) {
        return header1.getSellingMtn().equalsIgnoreCase(header2.getSellingMtn()) &&
                header1.getBillingId().equalsIgnoreCase(header2.getBillingId()) &&
                header1.getTradingDate().equals(header2.getTradingDate());
    }

    private BcqHeader findHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList) {
        for (BcqHeader header : headerList) {
            if (isSameHeader(header, headerToFind)) {
                return header;
            }
        }
        return new BcqHeader();
    }

    private List<BcqHeader> getExpiredHeadersByStatus(BcqStatus status) {
        return bcqDao.findAllHeaders(of(
                "expired", "expired",
                "status", status.toString()
        ));
    }

    private Map<Map<String, Object>, List<BcqHeader>> getGroupedHeaderList(List<BcqHeader> headerList) {
        return headerList.stream()
                .collect(groupingBy(header -> of(
                        "sellerShortName", header.getSellingParticipantShortName(),
                        "buyerShortName", header.getBuyingParticipantShortName(),
                        "status", header.getStatus(),
                        "tradingDate", header.getTradingDate()
                )));
    }

    private void updatePreviousHeaderStatus(BcqHeader header, BcqStatus newStatus) {
        List<BcqStatus> statusesToInclude = new ArrayList<>();
        statusesToInclude.add(CONFIRMED);
        if (newStatus == SETTLEMENT_READY) {
            statusesToInclude.add(SETTLEMENT_READY);
        }

        List<BcqHeader> sameHeaders = bcqDao.findSameHeaders(header, statusesToInclude, IN);
        if (sameHeaders.size() > 0) {
            BcqHeader prevHeader = sameHeaders.get(0);
            bcqDao.updateHeaderStatus(prevHeader.getHeaderId(), VOID);
        }
    }

    private void validateIfLatest(BcqHeader header) {
        List<BcqHeader> sameHeaders = bcqDao.findSameHeaders(header, singletonList(VOID), NOT_IN);
        if (sameHeaders.size() > 0) {
            BcqHeader latestHeader = sameHeaders.get(0);
            if (header.getHeaderId() != latestHeader.getHeaderId()) {
                throw new OldRecordException(NEW_VERSION_ERROR);
            }
        } else {
            throw new InvalidStateException(VOIDED_ERROR);
        }
    }

    private void validateUpdateStatus(BcqStatus oldStatus, BcqStatus newStatus) {
        if (asList(CONFIRMED, NULLIFIED, CANCELLED).contains(oldStatus)) {
            if (oldStatus == CONFIRMED && newStatus == CANCELLED) {
                return;
            }

            throw new InvalidStateException(UPDATED_ERROR);
        }
    }

    private void validateRequestForCancellation(BcqStatus oldStatus) {
        if (!asList(CONFIRMED, SETTLEMENT_READY).contains(oldStatus)) {
            throw new InvalidStateException(UPDATED_ERROR);
        }
    }

    private void validateApproval(BcqStatus oldStatus) {
        if (!asList(FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE, FOR_APPROVAL_CANCEL).contains(oldStatus)) {
            throw new InvalidStateException(UPDATED_ERROR);
        }
    }

    private void validateOverLapping(BcqProhibitedPair prohibitedPair, List<BcqProhibitedPair> bcqProhibitedPairs) {
        String pair = String.format("<b>%s</b> - <b>%s</b>.",
                prohibitedPair.getSellingMtn(), prohibitedPair.getBillingId());
        for (BcqProhibitedPair existingBcqProhibitedPair : bcqProhibitedPairs) {
            log.info("checking overlap date start date{} {}", existingBcqProhibitedPair.getEffectiveStartDate(), prohibitedPair.getEffectiveStartDate());
            if (DateTimeUtils.isBetweenInclusive(prohibitedPair.getEffectiveStartDate(), existingBcqProhibitedPair.getEffectiveStartDate(),
                    existingBcqProhibitedPair.getEffectiveEndDate())) {
                throw new IllegalArgumentException("Effective start date overlaps effective period of an existing pair "
                        + pair);
            } else if (DateTimeUtils.isBetweenInclusive(prohibitedPair.getEffectiveEndDate(), existingBcqProhibitedPair.getEffectiveStartDate(),
                    existingBcqProhibitedPair.getEffectiveEndDate())) {
                throw new IllegalArgumentException("Effective end date overlaps effective period of an existing pair "
                        + pair);
            } else if (DateTimeUtils.isBetweenInclusive(existingBcqProhibitedPair.getEffectiveStartDate(), prohibitedPair.getEffectiveStartDate(), prohibitedPair.getEffectiveEndDate())) {
                throw new IllegalArgumentException("Effective period overlaps effective start date of an existing pair "
                        + pair);
            } else if (DateTimeUtils.isBetweenInclusive(existingBcqProhibitedPair.getEffectiveEndDate(), prohibitedPair.getEffectiveStartDate(), prohibitedPair.getEffectiveEndDate())) {
                throw new IllegalArgumentException("Effective period overlaps effective end date of an existing pair "
                        + pair);
            } else if (existingBcqProhibitedPair.getEffectiveStartDate().isEqual(prohibitedPair.getEffectiveStartDate())) {
                throw new IllegalArgumentException("Effective start date overlaps effective period of an existing pair "
                        + pair);
            } else if (existingBcqProhibitedPair.getEffectiveEndDate() == null
                    && prohibitedPair.getEffectiveStartDate().isAfter(existingBcqProhibitedPair.getEffectiveStartDate())) {
                throw new IllegalArgumentException("Effective start date overlaps effective period of an existing pair "
                        + pair);
            } else if (existingBcqProhibitedPair.getEffectiveEndDate() == null && prohibitedPair.getEffectiveEndDate() == null
                    && prohibitedPair.getEffectiveStartDate().isBefore(existingBcqProhibitedPair.getEffectiveStartDate())) {
                throw new IllegalArgumentException("Effective start date overlaps effective period of an existing pair "
                        + pair + " Effective end date is Required!");
            }
        }
    }

}

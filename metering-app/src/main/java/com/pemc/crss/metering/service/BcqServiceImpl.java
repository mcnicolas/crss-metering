package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqEventCode;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.bcq.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqEventCode.*;
import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.*;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqServiceImpl implements BcqService {

    private final BcqDao bcqDao;
    private final BcqNotificationService bcqNotificationService;

    @Override
    @Transactional
    public void saveSellerDeclaration(BcqDeclaration declaration) {
        BcqUploadFile uploadFile = declaration.getUploadFileDetails().target();
        uploadFile.setFileId(saveUploadFile(uploadFile));
        if (uploadFile.getValidationStatus() == REJECTED) {
            sendValidationNotif(uploadFile, declaration);
            return;
        }
        List<BcqHeader> headerList = extractHeaderList(declaration);
        headerList = setUploadFileOfHeaders(headerList, uploadFile);
        headerList = setUpdatedViaOfHeaders(headerList, declaration, false);
        sendDeclarationNotif(bcqDao.saveHeaderList(headerList));
    }

    @Override
    public void saveSettlementDeclaration(BcqDeclaration declaration) {
        BcqUploadFile uploadFile = declaration.getUploadFileDetails().target();
        uploadFile.setFileId(saveUploadFile(uploadFile));
        if (uploadFile.getValidationStatus() == REJECTED) {
            sendValidationNotif(uploadFile, declaration);
        }
        List<BcqHeader> headerList = extractHeaderList(declaration);
        headerList = setUploadFileOfHeaders(headerList, uploadFile);
        headerList = setUpdatedViaOfHeaders(headerList, declaration, true);
        bcqDao.saveHeaderList(headerList);
    }

    @Override
    public Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest) {
        return bcqDao.findAllHeaders(pageableRequest);
    }

    @Override
    public List<BcqHeader> findAllHeadersBySellerAndTradingDate(String sellerShortName, Date tradingDate) {
        return bcqDao.findAllHeaders(of(
                "sellerName", sellerShortName,
                "tradingDate", "formatDate(tradingDate)"
        ));
    }

    @Override
    public List<ParticipantSellerDetails> findAllSellersWithExpiredBcqByTradingDate(Date tradingDate) {
        return bcqDao.findAllHeaders(of(
                "tradingDate", formatDate(tradingDate),
                "expired", "expired",
                "status", CONFIRMED.toString()
        )).stream().map(header ->
                new ParticipantSellerDetails(header.getSellingParticipantUserId(),
                        header.getSellingParticipantName(),
                        header.getSellingParticipantShortName()))
                .distinct()
                .collect(toList());
    }

    @Override
    public boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList) {
        for(BcqHeader header : headerList) {
            if (isSameHeader(header, headerToFind)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        return bcqDao.findHeader(headerId);
    }

    @Override
    public List<BcqData> findDataByHeaderId(long headerId) {
        return bcqDao.findDataByHeaderId(headerId);
    }

    @Override
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        bcqDao.updateHeaderStatus(headerId, status);
        sendUpdateStatusNotif(findHeader(headerId));
    }

    @Override
    @Transactional
    public void processUnconfirmedHeaders() {
        List<BcqHeader> unconfirmedHeaders = getExpiredHeadersByStatus(FOR_CONFIRMATION);
        unconfirmedHeaders.forEach(header -> bcqDao.updateHeaderStatus(header.getHeaderId(), NOT_CONFIRMED));
        Map<Map<String, Object>, List<BcqHeader>> groupedHeaders = getGroupedHeaderList(unconfirmedHeaders);
        groupedHeaders.forEach((map, headerList) -> sendUnprocessedNotif(headerList, NOT_CONFIRMED));
    }

    @Override
    @Transactional
    public void processUnnullifiedHeaders() {
        List<BcqHeader> unnullifiedHeaders = getExpiredHeadersByStatus(FOR_NULLIFICATION);
        unnullifiedHeaders.forEach(header -> bcqDao.updateHeaderStatus(header.getHeaderId(), CONFIRMED));
        Map<Map<String, Object>, List<BcqHeader>> groupedHeaders = getGroupedHeaderList(unnullifiedHeaders);
        groupedHeaders.forEach((map, headerList) -> sendUnprocessedNotif(headerList, CONFIRMED));
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private long saveUploadFile(BcqUploadFile uploadFile) {
        uploadFile.setSubmittedDate(new Date());
        uploadFile.setTransactionId(randomUUID().toString());
        return bcqDao.saveUploadFile(uploadFile);
    }

    private List<BcqHeader> extractHeaderList(BcqDeclaration declaration) {
        List<BcqHeader> headerList = new ArrayList<>();
        declaration.getHeaderDetailsList().forEach(headerDetails -> {
            ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
            BcqHeader header = headerDetails.target();
            header.setSellingParticipantUserId(sellerDetails.getUserId());
            header.setSellingParticipantName(sellerDetails.getName());
            header.setSellingParticipantShortName(sellerDetails.getShortName());
            headerList.add(header);
        });
        return headerList;
    }

    private List<BcqHeader> setUploadFileOfHeaders(List<BcqHeader> headerList, BcqUploadFile uploadFile) {
        return headerList.stream().map(header -> {
            header.setUploadFile(uploadFile);
            header.setFileId(uploadFile.getFileId());
            return header;
        }).collect(toList());
    }

    private List<BcqHeader> setUpdatedViaOfHeaders(List<BcqHeader> headerList, BcqDeclaration declaration,
                                                   boolean isSettlement) {

        if (declaration.isRedeclaration()) {
            List<BcqHeader> currentHeaderList = findAllHeadersBySellerAndTradingDate(
                    declaration.getSellerDetails().getShortName(),
                    declaration.getHeaderDetailsList().get(0).getTradingDate());
            return headerList.stream().map(header -> {
                boolean exists = isHeaderInList(header, currentHeaderList);
                header.setExists(exists);
                if (isSettlement) {
                    header.setUpdatedVia("MANUAL_OVERRIDE");
                } else {
                    if (exists) {
                        BcqHeader headerInList = findHeaderInList(header, currentHeaderList);
                        header.setHeaderId(headerInList.getHeaderId());
                        header.setUpdatedVia("REDECLARATION");
                    }
                }
                return header;
            }).collect(toList());
        }
        return headerList;
    }

    private boolean isSameHeader(BcqHeader header1, BcqHeader header2) {
        return header1.getSellingMtn().equals(header2.getSellingMtn()) &&
                header1.getBillingId().equals(header2.getBillingId()) &&
                header1.getTradingDate().equals(header2.getTradingDate());
    }

    private BcqHeader findHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList) {
        for(BcqHeader header : headerList) {
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
                        "sellerUserId", header.getSellingParticipantUserId(),
                        "buyerUserId", header.getBuyingParticipantUserId(),
                        "status", header.getStatus(),
                        "tradingDate", header.getTradingDate()
                )));
    }

    /****************************************************
     * NOTIF METHODS
     ****************************************************/
    private void sendValidationNotif(BcqUploadFile uploadFile, BcqDeclaration declaration) {
        ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
        bcqNotificationService.send(NTF_BCQ_VALIDATION_SELLER,
                formatDateTime(uploadFile.getSubmittedDate()),
                declaration.getValidationResult().getErrorMessage(),
                sellerDetails.getUserId());
        bcqNotificationService.send(NTF_BCQ_VALIDATION_DEPT,
                formatDateTime(uploadFile.getSubmittedDate()),
                sellerDetails.getName(),
                sellerDetails.getShortName(),
                declaration.getValidationResult().getErrorMessage());
    }

    private void sendDeclarationNotif(List<BcqHeader> headerList) {
        BcqHeader firstHeader = headerList.get(0);
        String submittedDate = formatDateTime(firstHeader.getUploadFile().getSubmittedDate());
        int recordCount = headerList.size() * firstHeader.getDataList().size();
        bcqNotificationService.send(NTF_BCQ_SUBMIT_SELLER, submittedDate, recordCount,
                firstHeader.getSellingParticipantUserId());
        for (BcqHeader header : headerList) {
            List<Object> payloadObjectList = new ArrayList<>();
            BcqEventCode code = NTF_BCQ_SUBMIT_BUYER;
            if (header.isExists()) {
                code = NTF_BCQ_UPDATE_BUYER;
                payloadObjectList.add(formatDate(firstHeader.getTradingDate()));
            }
            payloadObjectList.addAll(asList(submittedDate,
                    firstHeader.getSellingParticipantName(),
                    firstHeader.getSellingParticipantShortName(),
                    header.getHeaderId(),
                    header.getBuyingParticipantUserId()));
            bcqNotificationService.send(code, payloadObjectList.toArray());
        }
    }

    private void sendUpdateStatusNotif(BcqHeader header) {
        String respondedDate = formatLongDateTime(new Date());
        String tradingDate = formatLongDate(header.getTradingDate());
        List<Object> payloadObjectList = new ArrayList<>();
        payloadObjectList.addAll(asList(tradingDate, respondedDate, header.getHeaderId()));

        if (header.getStatus() == CANCELLED) {
            payloadObjectList.add(header.getSellingParticipantName());
            payloadObjectList.add(header.getSellingParticipantShortName());
            payloadObjectList.add(header.getBuyingParticipantUserId());
            bcqNotificationService.send(NTF_BCQ_CANCEL_BUYER, payloadObjectList.toArray());
        } else if (header.getStatus() == CONFIRMED || header.getStatus() == NULLIFIED) {
            payloadObjectList.add(header.getBuyingParticipantName());
            payloadObjectList.add(header.getBuyingParticipantShortName());
            payloadObjectList.add(header.getSellingParticipantUserId());
            if (header.getStatus() == CONFIRMED) {
                bcqNotificationService.send(NTF_BCQ_CONFIRM_SELLER, payloadObjectList.toArray());
            } else {
                bcqNotificationService.send(NTF_BCQ_NULLIFY_SELLER, payloadObjectList.toArray());
            }
        }
    }

    private void sendUnprocessedNotif(List<BcqHeader> headerList, BcqStatus status) {
        BcqHeader firstHeader = headerList.get(0);
        String deadlineDate = formatLongDateTime(firstHeader.getDeadlineDate());
        StringJoiner sellingMtns = new StringJoiner(", ");
        headerList.forEach(header -> sellingMtns.add(header.getSellingMtn()));
        bcqNotificationService.send(status == CONFIRMED ? NTF_BCQ_UNNULLIFIED_SELLER : NTF_BCQ_UNCONFIRMED_SELLER,
                formatDate(firstHeader.getTradingDate()),
                sellingMtns.toString(),
                firstHeader.getBuyingParticipantName(),
                firstHeader.getBuyingParticipantShortName(),
                firstHeader.getSellingParticipantUserId(),
                deadlineDate,
                status);
        bcqNotificationService.send(status == CONFIRMED ? NTF_BCQ_UNNULLIFIED_BUYER : NTF_BCQ_UNCONFIRMED_BUYER,
                formatDate(firstHeader.getTradingDate()),
                sellingMtns.toString(),
                firstHeader.getSellingParticipantName(),
                firstHeader.getSellingParticipantShortName(),
                firstHeader.getBuyingParticipantUserId(),
                deadlineDate,
                status);
    }

}

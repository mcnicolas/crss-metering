package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.bcq.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqServiceImpl implements BcqService {

    private final BcqDao bcqDao;
    private final BcqNotificationManager bcqNotificationManager;

    @Override
    @Transactional
    public void saveSellerDeclaration(BcqDeclaration declaration) {
        BcqUploadFile uploadFile = declaration.getUploadFileDetails().target();
        uploadFile.setFileId(saveUploadFile(uploadFile));
        if (uploadFile.getValidationStatus() == REJECTED) {
            bcqNotificationManager.sendValidationNotification(declaration, uploadFile.getSubmittedDate());
            return;
        }
        List<BcqHeader> headerList = extractHeaderList(declaration);
        headerList = setUploadFileOfHeaders(headerList, uploadFile);
        headerList = setUpdatedViaOfHeaders(headerList, declaration);
        bcqNotificationManager.sendUploadNotification(bcqDao.saveHeaderList(headerList));
    }

    @Override
    @Transactional
    public void saveSettlementDeclaration(BcqDeclaration declaration) {
        BcqUploadFile uploadFile = declaration.getUploadFileDetails().target();
        uploadFile.setFileId(saveUploadFile(uploadFile));
        if (uploadFile.getValidationStatus() == REJECTED) {
            bcqNotificationManager.sendSettlementValidationNotification(declaration, uploadFile.getSubmittedDate());
            return;
        }
        List<BcqHeader> headerList = extractHeaderList(declaration);
        headerList = setUploadFileOfHeaders(headerList, uploadFile);
        headerList = setUpdatedViaOfHeadersBySettlement(headerList, declaration);
        bcqNotificationManager.sendSettlementUploadNotification(bcqDao.saveHeaderList(headerList));
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
                "expired", "expired"
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
    @Transactional
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        bcqDao.updateHeaderStatus(headerId, status);
        bcqNotificationManager.sendUpdateStatusNotification(findHeader(headerId));
    }

    @Override
    @Transactional
    public void updateHeaderStatusBySettlement(long headerId, BcqStatus status) {
        bcqDao.updateHeaderStatusBySettlement(headerId, status);
        bcqNotificationManager.sendSettlementUpdateStatusNotification(findHeader(headerId));
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
        unnullifiedHeaders.forEach(header -> bcqDao.updateHeaderStatus(header.getHeaderId(), CONFIRMED));
        Map<Map<String, Object>, List<BcqHeader>> groupedHeaders = getGroupedHeaderList(unnullifiedHeaders);
        groupedHeaders.forEach((map, headerList) -> bcqNotificationManager
                .sendUnprocessedNotification(headerList, CONFIRMED));
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

    private List<BcqHeader> setUpdatedViaOfHeaders(List<BcqHeader> headerList, BcqDeclaration declaration) {
        if (declaration.isRedeclaration()) {
            List<BcqHeader> currentHeaderList = findAllHeadersBySellerAndTradingDate(
                    declaration.getSellerDetails().getShortName(),
                    declaration.getHeaderDetailsList().get(0).getTradingDate());
            return headerList.stream().map(header -> {
                boolean exists = isHeaderInList(header, currentHeaderList);
                header.setExists(exists);
                if (exists) {
                    BcqHeader headerInList = findHeaderInList(header, currentHeaderList);
                    header.setHeaderId(headerInList.getHeaderId());
                    header.setUpdatedVia("REDECLARATION");
                }
                return header;
            }).collect(toList());
        }
        return headerList;
    }

    private List<BcqHeader> setUpdatedViaOfHeadersBySettlement(List<BcqHeader> headerList, BcqDeclaration declaration) {
        List<BcqHeader> currentHeaderList = findAllHeadersBySellerAndTradingDate(
                declaration.getSellerDetails().getShortName(),
                declaration.getHeaderDetailsList().get(0).getTradingDate());
        return headerList.stream().map(header -> {
            boolean exists = isHeaderInList(header, currentHeaderList);
            if (exists) {
                BcqHeader headerInList = findHeaderInList(header, currentHeaderList);
                header.setHeaderId(headerInList.getHeaderId());
                if (headerInList.getStatus() == FOR_APPROVAL_NEW) {
                    header.setStatus(FOR_APPROVAL_NEW);
                } else {
                    header.setStatus(FOR_APPROVAL_UPDATED);
                }
                header.setExists(true);
            } else {
                header.setStatus(FOR_APPROVAL_NEW);
            }
            header.setUpdatedVia("MANUAL_OVERRIDE");
            return header;
        }).collect(toList());
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

}

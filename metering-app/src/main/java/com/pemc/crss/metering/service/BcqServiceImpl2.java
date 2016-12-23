package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.BcqEventCode;
import com.pemc.crss.metering.dao.BcqDao2;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.pemc.crss.metering.constants.BcqEventCode.*;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDateTime;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqServiceImpl2 implements BcqService2 {

    private final BcqDao2 bcqDao;
    private final BcqNotificationService bcqNotificationService;

    @Override
    @Transactional
    public long saveUploadFile(BcqUploadFile uploadFile) {
        uploadFile.setTransactionId(randomUUID().toString());
        uploadFile.setValidationStatus(ACCEPTED);
        return bcqDao.saveUploadFile(uploadFile);
    }

    @Override
    @Transactional
    public void saveFailedUploadFile(BcqUploadFile uploadFile, BcqDeclaration declaration) {
        bcqDao.saveUploadFile(uploadFile);
        sendValidationNotif(uploadFile, declaration);
    }

    @Override
    @Transactional
    public void saveDeclaration(BcqDeclaration declaration) {
        BcqUploadFile uploadFile = declaration.getUploadFileDetails().target();
        uploadFile.setFileId(saveUploadFile(uploadFile));
        List<BcqHeader> currentHeaderList = new ArrayList<>();
        List<BcqHeader> headerList = new ArrayList<>();
        if (declaration.isRedeclaration()) {
            currentHeaderList.addAll(findAllHeadersBySellerAndTradingDate(declaration.getSellerDetails().getShortName(),
                    declaration.getHeaderDetailsList().get(0).getTradingDate()));
        }
        declaration.getHeaderDetailsList().forEach(headerDetails -> {
            ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
            BcqHeader header = headerDetails.target();
            header.setFileId(uploadFile.getFileId());
            header.setUploadFile(uploadFile);
            header.setSellingParticipantUserId(sellerDetails.getUserId());
            header.setSellingParticipantName(sellerDetails.getName());
            header.setSellingParticipantShortName(sellerDetails.getShortName());

            boolean exists = isHeaderInList(header, currentHeaderList);
            header.setExists(exists);
            if (exists) {
                header.setHeaderId(findHeaderInList(header, currentHeaderList).getHeaderId());
                header.setUpdatedVia("REDECLARATION");
            }
            headerList.add(header);
        });
        sendDeclarationNotif(bcqDao.saveHeaderList(headerList));
    }

    @Override
    public List<BcqHeader> findAllHeadersBySellerAndTradingDate(String sellerShortName, Date tradingDate) {
        Map<String, String> params = new HashMap<>();
        params.put("sellerName", sellerShortName);
        params.put("tradingDate", formatDate(tradingDate));
        return bcqDao.findAllHeaders(params);
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

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
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
        return null;
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
}

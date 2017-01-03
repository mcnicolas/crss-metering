package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqEventCode;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.pemc.crss.metering.constants.BcqEventCode.*;
import static com.pemc.crss.metering.constants.BcqStatus.CANCELLED;
import static com.pemc.crss.metering.constants.BcqStatus.CONFIRMED;
import static com.pemc.crss.metering.constants.BcqStatus.NULLIFIED;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.*;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqServiceImpl implements BcqService {

    private final BcqDao bcqDao;
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
            currentHeaderList.addAll(findAllHeadersBySellerAndTradingDate(
                    declaration.getSellerDetails().getShortName(),
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
                BcqHeader headerInList = findHeaderInList(header, currentHeaderList);
                if (headerInList != null) {
                    header.setHeaderId(headerInList.getHeaderId());
                    header.setUpdatedVia("REDECLARATION");
                }
            }
            headerList.add(header);
        });
        sendDeclarationNotif(bcqDao.saveHeaderList(headerList));
    }

    @Override
    public Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest) {
        return bcqDao.findAllHeaders(pageableRequest);
    }

    @Override
    public List<BcqHeader> findAllHeadersBySellerAndTradingDate(String sellerShortName, Date tradingDate) {
        Map<String, String> params = new HashMap<>();
        params.put("sellerName", sellerShortName);
        params.put("tradingDate", formatDate(tradingDate));
        return bcqDao.findAllHeaders(params);
    }

    @Override
    public List<ParticipantSellerDetails> findAllSellersByTradingDate(Date tradingDate) {
        Map<String, String> params = new HashMap<>();
        params.put("tradingDate", formatDate(tradingDate));
        return bcqDao.findAllHeaders(params).stream()
                .map(header ->
                        new ParticipantSellerDetails(header.getSellingParticipantUserId(),
                                header.getSellingParticipantName(),
                                header.getSellingParticipantShortName()))
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

}

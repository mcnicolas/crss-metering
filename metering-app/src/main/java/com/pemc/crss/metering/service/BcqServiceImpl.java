package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqEventCode.*;
import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static java.util.Arrays.asList;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BcqServiceImpl implements BcqService {

    private final BcqDao bcqDao;
    private final BcqNotificationService notificationService;

    @Autowired
    public BcqServiceImpl(BcqDao bcqDao, BcqNotificationService notificationService) {
        this.bcqDao = bcqDao;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void save(BcqDetails details) {
        BcqUploadFile file = details.getFile();
        String format = "MMM. dd, yyyy hh:mm a";
        DateFormat dateFormat = new SimpleDateFormat(format);
        String submittedDate = dateFormat.format(file.getSubmittedDate());
        String transactionId = UUID.randomUUID().toString();

        if (details.getErrorMessage() != null) {
            file.setValidationStatus(REJECTED);
            sendValidationError(details, submittedDate);
            bcqDao.saveUploadFile(transactionId, file);
        } else {
            file.setValidationStatus(ACCEPTED);

            List<BcqHeader> headerList = details.getHeaderList();
            List<Long> buyerIds = details.getBuyerIds();
            Long sellerId = details.getSellerId();
            BcqHeader header = headerList.get(0);
            long fileId = bcqDao.saveUploadFile(transactionId, file);
            List<Long> headerIds = bcqDao.saveBcq(fileId, headerList);
            int recordCount = headerList.size() * header.getDataList().size();

            for (int i = 0; i < headerIds.size(); i ++) {
                notificationService.send(NTF_BCQ_SUBMIT_BUYER,
                        submittedDate,
                        header.getSellingParticipantName(),
                        header.getSellingParticipantShortName(),
                        headerIds.get(i),
                        buyerIds.get(i));
            }
            notificationService.send(NTF_BCQ_SUBMIT_SELLER, submittedDate, recordCount, sellerId);
        }
    }

    @Override
    public List<BcqHeader> findAllHeaders(Map<String, String> params) {
        return bcqDao.findAllHeaders(params);
    }

    @Override
    public Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest) {
        return bcqDao.findAllHeaders(pageableRequest);
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        return bcqDao.findHeader(headerId);
    }

    @Override
    public List<BcqData> findAllData(long headerId) {
        return bcqDao.findAllBcqData(headerId);
    }

    @Override
    @Transactional
    public void updateHeaderStatus(long headerId, BcqUpdateStatusDetails updateStatusDetails) {
        bcqDao.updateHeaderStatus(headerId, updateStatusDetails.getStatus());
        DateFormat dateTimeFormat = new SimpleDateFormat("MMM. dd, yyyy hh:mm a");
        DateFormat dateFormat = new SimpleDateFormat("MMM. dd, yyyy");
        String respondedDate = dateTimeFormat.format(new Date());
        String tradingDate = dateFormat.format(updateStatusDetails.getTradingDate());

        List<Object> payloadObjectList = new ArrayList<>();
        payloadObjectList.addAll(asList(tradingDate, respondedDate, headerId));

        BcqStatus status = updateStatusDetails.getStatus();
        if (status == CANCELLED) {
            payloadObjectList.add(updateStatusDetails.getSellerName());
            payloadObjectList.add(updateStatusDetails.getSellerShortName());
            payloadObjectList.add(updateStatusDetails.getBuyerId());
            notificationService.send(NTF_BCQ_CANCEL_BUYER, payloadObjectList.toArray());
        } else if (status == CONFIRMED || status == NULLIFIED) {
            payloadObjectList.add(updateStatusDetails.getBuyerName());
            payloadObjectList.add(updateStatusDetails.getBuyerShortName());
            payloadObjectList.add(updateStatusDetails.getSellerId());
            if (status == CONFIRMED) {
                notificationService.send(NTF_BCQ_CONFIRM_SELLER, payloadObjectList.toArray());
            } else {
                notificationService.send(NTF_BCQ_NULLIFY_SELLER, payloadObjectList.toArray());
            }
        }
    }

    @Override
    public boolean headerExists(BcqHeader header) {
        return bcqDao.headerExists(header);
    }

    private void sendValidationError(BcqDetails details, String submittedDate) {
        notificationService.send(NTF_BCQ_VALIDATION_SELLER,
                submittedDate,
                details.getErrorMessage(),
                details.getSellerId());

        notificationService.send(NTF_BCQ_VALIDATION_DEPT,
                submittedDate,
                details.getSellerName(),
                details.getSellerShortName(),
                details.getErrorMessage());
    }

}

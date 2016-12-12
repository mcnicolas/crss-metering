package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUpdateStatusDetails;
import com.pemc.crss.metering.dto.BcqUploadFile;
import com.pemc.crss.metering.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqStatus.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BcqServiceImpl implements BcqService {

    private final BcqDao bcqDao;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public BcqServiceImpl(BcqDao bcqDao, ApplicationEventPublisher eventPublisher) {
        this.bcqDao = bcqDao;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void saveBcq(BcqUploadFile file, List<BcqHeader> headerList, List<Long> buyerIds, Long sellerId) {
        String transactionId = UUID.randomUUID().toString();
        long fileId = bcqDao.saveUploadFile(transactionId, file);
        List<Long> headerIds = bcqDao.saveBcq(fileId, headerList);
        Map<String, Object> payload = new HashMap<>();
        String format = "MMM. dd, yyyy hh:mm";
        DateFormat dateFormat = new SimpleDateFormat(format);
        String submittedDate = dateFormat.format(file.getSubmittedDate());
        int recordCount = headerList.size() * headerList.get(0).getDataList().size();

        payload.put("submittedDate", submittedDate);
        payload.put("sellerName", headerList.get(0).getSellingParticipantName());
        payload.put("sellerShortName", headerList.get(0).getSellingParticipantShortName());
        for (int i = 0; i < headerIds.size(); i ++) {
            payload.put("recordCount", recordCount);
            payload.put("headerId", headerIds.get(i));
            payload.put("recipientId", buyerIds.get(i));
            eventPublisher.publishEvent(new BcqSubmitBuyerEvent(payload));
        }
        payload.put("recipientId", sellerId);
        eventPublisher.publishEvent(new BcqSubmitSellerEvent(payload));
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
        Map<String, Object> payload = new HashMap<>();
        DateFormat dateTimeFormat = new SimpleDateFormat("MMM. dd, yyyy hh:mm");
        DateFormat dateFormat = new SimpleDateFormat("MMM. dd, yyyy");
        String respondedDate = dateTimeFormat.format(new Date());
        String tradingDate = dateFormat.format(updateStatusDetails.getTradingDate());

        payload.put("headerId", headerId);
        payload.put("respondedDate", respondedDate);
        payload.put("tradingDate", tradingDate);

        BcqEvent event = null;
        BcqStatus status = updateStatusDetails.getStatus();
        if (status == CANCELLED) {
            payload.put("recipientId", updateStatusDetails.getBuyerId());
            payload.put("sellerName", updateStatusDetails.getSellerName());
            payload.put("sellerShortName", updateStatusDetails.getSellerShortName());
            event = new BcqStatusCancelEvent(payload);
        } else if (status == CONFIRMED || status == NULLIFIED) {
            payload.put("recipientId", updateStatusDetails.getSellerId());
            payload.put("buyerName", updateStatusDetails.getBuyerName());
            payload.put("buyerShortName", updateStatusDetails.getBuyerShortName());
            if (status == CONFIRMED) {
                event = new BcqStatusConfirmEvent(payload);
            } else {
                event = new BcqStatusNullifyEvent(payload);
            }
        }
        eventPublisher.publishEvent(event);
    }

    @Override
    public boolean headerExists(BcqHeader header) {
        return bcqDao.headerExists(header);
    }

}

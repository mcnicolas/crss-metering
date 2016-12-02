package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.event.BcqUploadEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.pemc.crss.metering.constants.BcqNotificationRecipient.BUYER;
import static com.pemc.crss.metering.constants.BcqNotificationRecipient.SELLER;
import static com.pemc.crss.metering.constants.BcqNotificationType.SUBMIT;
import static com.pemc.crss.metering.constants.BcqUploadEventCode.NTF_BCQ_SUBMIT_BUYER;
import static com.pemc.crss.metering.constants.BcqUploadEventCode.NTF_BCQ_SUBMIT_SELLER;

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

        payload.put("submittedDate", submittedDate);
        payload.put("recordCount", headerList.size() * headerList.get(0).getDataList().size());
        payload.put("sellerName",
                headerList.get(0).getSellingParticipantName());
        payload.put("sellerShortName",
                headerList.get(0).getSellingParticipantShortName());

        for (int i = 0; i < headerIds.size(); i ++) {
            payload.put("headerId", headerIds.get(i));
            payload.put("buyerId", buyerIds.get(i));

            BcqUploadEvent event = new BcqUploadEvent(payload, NTF_BCQ_SUBMIT_BUYER, SUBMIT, BUYER);
            eventPublisher.publishEvent(event);
        }

        payload.put("sellerId", sellerId);
        BcqUploadEvent event = new BcqUploadEvent(payload, NTF_BCQ_SUBMIT_SELLER, SUBMIT, SELLER);
        eventPublisher.publishEvent(event);
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
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        bcqDao.updateHeaderStatus(headerId, status);
    }
}

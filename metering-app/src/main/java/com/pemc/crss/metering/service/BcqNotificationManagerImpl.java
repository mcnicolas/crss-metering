package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.BcqEventCode;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import com.pemc.crss.metering.event.BcqEvent;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.pemc.crss.metering.constants.BcqEventCode.*;
import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDateTime;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqNotificationManagerImpl implements BcqNotificationManager {

    private final ApplicationEventPublisher eventPublisher;
    private final ResourceTemplate resourceTemplate;

    private static final String DEPT_BILLING = "BILLING";
    private static final String NOTIF_URL = "/reg/bcq/notif/%s/get-user-ids";

    @Override
    public void sendValidationNotification(BcqDeclaration declaration, Date submittedDate) {
        ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
        String formattedSubmittedDate = formatLongDateTime(submittedDate);
        Set<Integer> sellerUserIds = getUserIdsByShortName(sellerDetails.getShortName());
        sellerUserIds.forEach(sellerUserId ->
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(NTF_BCQ_VALIDATION_SELLER.toString())
                        .withRecipientId(sellerUserId)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("errorMessage", declaration.getValidationResult().getErrorMessage()
                                .getFormattedMessage())
                        .build())));
        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                .withCode(NTF_BCQ_VALIDATION_DEPT.toString())
                .withRecipientDeptCode(DEPT_BILLING)
                .addLoad("submittedDate", formattedSubmittedDate)
                .addLoad("sellerName", sellerDetails.getName())
                .addLoad("sellerShortName", sellerDetails.getShortName())
                .addLoad("errorMessage", declaration.getValidationResult().getErrorMessage().getFormattedMessage())
                .build()));
    }

    @Override
    public void sendUploadNotification(List<BcqHeader> headerList) {
        BcqHeader firstHeader = headerList.get(0);
        String formattedSubmittedDate = formatLongDateTime(firstHeader.getUploadFile().getSubmittedDate());
        int recordCount = headerList.size() * firstHeader.getDataList().size();
        String sellerName = firstHeader.getSellingParticipantName();
        String sellerShortName = firstHeader.getSellingParticipantShortName();
        Set<Integer> sellerUserIds = getUserIdsByShortName(sellerShortName);
        sellerUserIds.forEach(sellerUserId ->
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(NTF_BCQ_SUBMIT_SELLER.toString())
                        .withRecipientId(sellerUserId)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("recordCount", recordCount)
                        .build())));
        for (BcqHeader header : headerList) {
            Set<Integer> buyerUserIds = getUserIdsByShortName(header.getBuyingParticipantShortName());
            buyerUserIds.forEach(buyerUserId -> {
                BcqEventCode code = NTF_BCQ_SUBMIT_BUYER;
                String formattedTradingDate = null;
                if (header.isExists()) {
                    code = NTF_BCQ_UPDATE_BUYER;
                    formattedTradingDate = formatLongDate(firstHeader.getTradingDate());
                }
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(code.toString())
                        .withRecipientId(buyerUserId)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("sellerName", sellerName)
                        .addLoad("sellerShortName", sellerShortName)
                        .addLoad("headerId", header.getHeaderId())
                        .build()));
            });
        }
    }

    @Override
    public void sendSettlementValidationNotification(BcqDeclaration declaration, Date submittedDate) {
        String formattedSubmittedDate = formatLongDateTime(submittedDate);
        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                .withCode(NTF_BCQ_SETTLEMENT_VALIDATION_DEPT.toString())
                .withRecipientDeptCode(DEPT_BILLING)
                .addLoad("submittedDate", formattedSubmittedDate)
                .addLoad("settlementUser", getSettlementName())
                .addLoad("errorMessage", declaration.getValidationResult().getErrorMessage().getFormattedMessage())
                .build()));
    }

    @Override
    public void sendSettlementUploadNotification(List<BcqHeader> headerList) {
        BcqHeader firstHeader = headerList.get(0);
        String settlementUser = getSettlementName();
        String formattedTradingDate = formatLongDate(firstHeader.getTradingDate());
        String formattedSubmittedDate = formatLongDateTime(firstHeader.getUploadFile().getSubmittedDate());
        for (BcqHeader header : headerList) {
            if (header.isExists() && header.getStatus() != FOR_APPROVAL_NEW) {
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(NTF_BCQ_SETTLEMENT_UPDATE_DEPT.toString())
                        .withRecipientDeptCode(DEPT_BILLING)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("settlementUser", settlementUser)
                        .addLoad("headerId", header.getHeaderId())
                        .build()));
            } else {
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(NTF_BCQ_SETTLEMENT_NEW_DEPT.toString())
                        .withRecipientDeptCode(DEPT_BILLING)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("settlementUser", settlementUser)
                        .addLoad("sellerName", firstHeader.getSellingParticipantName())
                        .addLoad("sellerShortName", firstHeader.getSellingParticipantShortName())
                        .addLoad("headerId", header.getHeaderId())
                        .build()));
            }
        }
    }

    @Override
    public void sendUpdateStatusNotification(BcqHeader header) {
        String formattedRespondedDate = formatLongDateTime(new Date());
        String formattedTradingDate = formatLongDate(header.getTradingDate());
        if (header.getStatus() == CANCELLED) {
            Set<Integer> buyerUserIds = getUserIdsByShortName(header.getBuyingParticipantShortName());
            buyerUserIds.forEach(buyerUserId ->
                    eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                            .withCode(NTF_BCQ_CANCEL_BUYER.toString())
                            .withRecipientId(buyerUserId)
                            .addLoad("tradingDate", formattedTradingDate)
                            .addLoad("respondedDate", formattedRespondedDate)
                            .addLoad("sellerName", header.getSellingParticipantName())
                            .addLoad("sellerShortName", header.getSellingParticipantShortName())
                            .addLoad("headerId", header.getHeaderId())
                            .build())));

        } else {
            BcqEventCode code = header.getStatus() == CONFIRMED ? NTF_BCQ_CONFIRM_SELLER : NTF_BCQ_NULLIFY_SELLER;
            Set<Integer> sellerUserIds = getUserIdsByShortName(header.getSellingParticipantShortName());
            sellerUserIds.forEach(sellerUserId ->
                    eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                            .withCode(code.toString())
                            .withRecipientId(sellerUserId)
                            .addLoad("tradingDate", formattedTradingDate)
                            .addLoad("respondedDate", formattedRespondedDate)
                            .addLoad("buyerName", header.getBuyingParticipantName())
                            .addLoad("buyerShortName", header.getBuyingParticipantShortName())
                            .addLoad("headerId", header.getHeaderId())
                            .build())));
        }
    }

    @Override
    public void sendSettlementUpdateStatusNotification(BcqHeader header) {
        String formattedRespondedDate = formatLongDateTime(new Date());
        String formattedTradingDate = formatLongDate(header.getTradingDate());
        if (header.getStatus() == FOR_APPROVAL_CANCEL) {
            eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                    .withCode(NTF_BCQ_REQUEST_CANCEL_DEPT.toString())
                    .withRecipientDeptCode(DEPT_BILLING)
                    .addLoad("tradingDate", formattedTradingDate)
                    .addLoad("respondedDate", formattedRespondedDate)
                    .addLoad("settlementUser", getSettlementName())
                    .addLoad("headerId", header.getHeaderId())
                    .build()));
        }
    }

    @Override
    public void sendApprovalNotification(BcqHeader header) {
        String settlementUser = getSettlementName();
        String formattedTradingDate = formatLongDate(header.getTradingDate());
        String formattedSubmittedDate = formatLongDateTime(header.getUploadFile().getSubmittedDate());
        String formattedRespondedDate = formatLongDateTime(new Date());
        Set<Integer> sellerUserIds = getUserIdsByShortName(header.getSellingParticipantShortName());
        Set<Integer> buyerUserIds = getUserIdsByShortName(header.getBuyingParticipantShortName());
        switch (header.getStatus()) {
            case FOR_APPROVAL_UPDATED:
                sellerUserIds.forEach(sellerUserId ->
                    eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                            .withCode(NTF_BCQ_APPROVE_UPDATE_SELLER.toString())
                            .withRecipientId(sellerUserId)
                            .addLoad("tradingDate", formattedTradingDate)
                            .addLoad("submittedDate", formattedSubmittedDate)
                            .addLoad("settlementUser", getSettlementName())
                            .addLoad("headerId", header.getHeaderId())
                            .build())));

                buyerUserIds.forEach(buyerUserId ->
                        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                                .withCode(NTF_BCQ_APPROVE_UPDATE_BUYER.toString())
                                .withRecipientId(buyerUserId)
                                .addLoad("tradingDate", formattedTradingDate)
                                .addLoad("submittedDate", formattedSubmittedDate)
                                .addLoad("settlementUser", getSettlementName())
                                .addLoad("headerId", header.getHeaderId())
                                .build())));
                break;
            case FOR_APPROVAL_NEW:
                sellerUserIds.forEach(sellerUserId ->
                        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                                .withCode(NTF_BCQ_APPROVE_NEW_SELLER.toString())
                                .withRecipientId(sellerUserId)
                                .addLoad("submittedDate", formattedSubmittedDate)
                                .addLoad("buyerName", header.getBuyingParticipantName())
                                .addLoad("buyerShortName", header.getBuyingParticipantShortName())
                                .addLoad("settlementUser", getSettlementName())
                                .addLoad("headerId", header.getHeaderId())
                                .build())));

                buyerUserIds.forEach(buyerUserId ->
                        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                                .withCode(NTF_BCQ_APPROVE_NEW_BUYER.toString())
                                .withRecipientId(buyerUserId)
                                .addLoad("submittedDate", formattedSubmittedDate)
                                .addLoad("sellerName", header.getSellingParticipantName())
                                .addLoad("sellerShortName", header.getSellingParticipantShortName())
                                .addLoad("settlementUser", getSettlementName())
                                .addLoad("headerId", header.getHeaderId())
                                .build())));
                break;
            case FOR_APPROVAL_CANCEL:
                sellerUserIds.forEach(sellerUserId ->
                        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                                .withCode(NTF_BCQ_APPROVE_CANCEL_SELLER.toString())
                                .withRecipientId(sellerUserId)
                                .addLoad("tradingDate", formattedTradingDate)
                                .addLoad("respondedDate", formattedRespondedDate)
                                .addLoad("settlementUser", settlementUser)
                                .addLoad("headerId", header.getHeaderId())
                                .build())));
                buyerUserIds.forEach(buyerUserId ->
                        eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                                .withCode(NTF_BCQ_APPROVE_CANCEL_BUYER.toString())
                                .withRecipientId(buyerUserId)
                                .addLoad("tradingDate", formattedTradingDate)
                                .addLoad("respondedDate", formattedRespondedDate)
                                .addLoad("settlementUser", settlementUser)
                                .addLoad("headerId", header.getHeaderId())
                                .build())));
            default:
                break;
        }
    }

    @Override
    public void sendUnprocessedNotification(List<BcqHeader> headerList, BcqStatus status) {
        BcqHeader firstHeader = headerList.get(0);
        String formattedTradingDate = formatLongDate(firstHeader.getTradingDate());
        String formattedDeadlineDate = formatLongDateTime(firstHeader.getDeadlineDate());
        StringJoiner sellingMtns = new StringJoiner(", ");
        headerList.forEach(header -> sellingMtns.add(header.getSellingMtn()));
        BcqEventCode sellerCode = status == CONFIRMED ? NTF_BCQ_UNNULLIFIED_SELLER : NTF_BCQ_UNCONFIRMED_SELLER;
        BcqEventCode buyerCode = status == CONFIRMED ? NTF_BCQ_UNNULLIFIED_BUYER : NTF_BCQ_UNCONFIRMED_BUYER;
        Set<Integer> sellerUserIds = getUserIdsByShortName(firstHeader.getSellingParticipantShortName());
        Set<Integer> buyerUserIds = getUserIdsByShortName(firstHeader.getBuyingParticipantShortName());

        sellerUserIds.forEach(sellerUserId ->
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(sellerCode.toString())
                        .withRecipientId(sellerUserId)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("sellingMtns", sellingMtns.toString())
                        .addLoad("buyerName", firstHeader.getBuyingParticipantName())
                        .addLoad("buyerShortName", firstHeader.getBuyingParticipantShortName())
                        .addLoad("deadlineDate", formattedDeadlineDate)
                        .addLoad("status", status)
                        .build())));

        buyerUserIds.forEach(buyerUserId ->
                eventPublisher.publishEvent(new BcqEvent(new NotificationBuilder()
                        .withCode(buyerCode.toString())
                        .withRecipientId(buyerUserId)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("sellingMtns", sellingMtns.toString())
                        .addLoad("sellerName", firstHeader.getSellingParticipantName())
                        .addLoad("sellerShortName", firstHeader.getSellingParticipantShortName())
                        .addLoad("deadlineDate", formattedDeadlineDate)
                        .addLoad("status", status)
                        .build())));
    }

    //TODO Find a better approach for this
    @SuppressWarnings("unchecked")
    private String getSettlementName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LinkedHashMap<String, Object> oAuthPrincipal = (LinkedHashMap) auth.getPrincipal();
        LinkedHashMap<String, Object> userAuthentication = (LinkedHashMap) oAuthPrincipal.get("userAuthentication");
        LinkedHashMap<String, String> principal = (LinkedHashMap) userAuthentication.get("principal");
        return principal.get("firstName") + " " + principal.get("lastName") + " (" + principal.get("username") + ")";
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getUserIdsByShortName(String shortName) {
        log.debug("CLASS: {}", resourceTemplate.get(String.format(NOTIF_URL, shortName), Set.class).getClass().getClass());
        return resourceTemplate.get(String.format(NOTIF_URL, shortName), Set.class);
    }

}

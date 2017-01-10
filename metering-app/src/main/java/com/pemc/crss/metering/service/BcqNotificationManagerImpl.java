package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.BcqEventCode;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import com.pemc.crss.metering.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

import static com.pemc.crss.metering.constants.BcqEventCode.*;
import static com.pemc.crss.metering.constants.BcqStatus.CANCELLED;
import static com.pemc.crss.metering.constants.BcqStatus.CONFIRMED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDateTime;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDateTime;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqNotificationManagerImpl implements BcqNotificationManager {

    private final NotificationService notificationService;

    private static final String DEPT_BILLING = "BILLING";

    @Override
    public void sendValidationNotification(BcqDeclaration declaration, Date submittedDate) {
        ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
        String formattedSubmittedDate = formatLongDateTime(submittedDate);
        notificationService.notify(new NotificationBuilder()
                .withCode(NTF_BCQ_VALIDATION_SELLER.toString())
                .withRecipientId(sellerDetails.getUserId())
                .addLoad("submittedDate", formattedSubmittedDate)
                .addLoad("errorMessage", declaration.getValidationResult().getErrorMessage())
                .build());
        notificationService.notify(new NotificationBuilder()
                .withCode(NTF_BCQ_VALIDATION_SELLER.toString())
                .withRecipientId(sellerDetails.getUserId())
                .addLoad("submittedDate", formattedSubmittedDate)
                .addLoad("errorMessage", declaration.getValidationResult().getErrorMessage())
                .build());
    }

    @Override
    public void sendUploadNotification(List<BcqHeader> headerList) {
        BcqHeader firstHeader = headerList.get(0);
        String formattedSubmittedDate = formatLongDateTime(firstHeader.getUploadFile().getSubmittedDate());
        int recordCount = headerList.size() * firstHeader.getDataList().size();
        String sellerName = firstHeader.getSellingParticipantName();
        String sellerShortName = firstHeader.getSellingParticipantShortName();
        notificationService.notify(new NotificationBuilder()
                .withCode(NTF_BCQ_SUBMIT_SELLER.toString())
                .withRecipientId(firstHeader.getSellingParticipantUserId())
                .addLoad("submittedDate", formattedSubmittedDate)
                .addLoad("recordCount", recordCount)
                .build());
        for (BcqHeader header : headerList) {
            BcqEventCode code = NTF_BCQ_SUBMIT_BUYER;
            String formattedTradingDate = null;
            if (header.isExists()) {
                code = NTF_BCQ_UPDATE_BUYER;
                formattedTradingDate = formatLongDate(firstHeader.getTradingDate());
            }
            notificationService.notify(new NotificationBuilder()
                    .withCode(code.toString())
                    .withRecipientId(header.getBuyingParticipantUserId())
                    .addLoad("tradingDate", formattedTradingDate)
                    .addLoad("submittedDate", formattedSubmittedDate)
                    .addLoad("sellerName", sellerName)
                    .addLoad("sellerShortName", sellerShortName)
                    .addLoad("headerId", header.getHeaderId())
                    .build());
        }
    }

    @Override
    public void sendSettlementUploadNotification(List<BcqHeader> headerList) {
        BcqHeader firstHeader = headerList.get(0);
        String settlementUser = getSettlementName();
        String formattedTradingDate = formatDateTime(firstHeader.getTradingDate());
        String formattedSubmittedDate = formatLongDateTime(firstHeader.getUploadFile().getSubmittedDate());
        for (BcqHeader header : headerList) {
            if (header.isExists()) {
                notificationService.notify(new NotificationBuilder()
                        .withCode(NTF_BCQ_SETTLEMENT_UPDATE_DEPT.toString())
                        .withRecipientDeptCode(DEPT_BILLING)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("settlementUser", settlementUser)
                        .addLoad("headerId", header.getHeaderId())
                        .build());
            } else {
                notificationService.notify(new NotificationBuilder()
                        .withCode(NTF_BCQ_SETTLEMENT_NEW_DEPT.toString())
                        .withRecipientDeptCode(DEPT_BILLING)
                        .addLoad("tradingDate", formattedTradingDate)
                        .addLoad("submittedDate", formattedSubmittedDate)
                        .addLoad("settlementUser", settlementUser)
                        .addLoad("sellerName", firstHeader.getSellingParticipantName())
                        .addLoad("sellerShortName", firstHeader.getSellingParticipantShortName())
                        .addLoad("headerId", header.getHeaderId())
                        .build());
            }
        }
    }

    @Override
    public void sendUpdateStatusNotification(BcqHeader header) {
        String formattedRespondedDate = formatLongDateTime(new Date());
        String formattedTradingDate = formatLongDate(header.getTradingDate());
        if (header.getStatus() == CANCELLED) {
            notificationService.notify(new NotificationBuilder()
                    .withCode(NTF_BCQ_CANCEL_BUYER.toString())
                    .withRecipientId(header.getBuyingParticipantUserId())
                    .addLoad("tradingDate", formattedTradingDate)
                    .addLoad("respondedDate", formattedRespondedDate)
                    .addLoad("sellerName", header.getSellingParticipantName())
                    .addLoad("sellerShortName", header.getSellingParticipantShortName())
                    .addLoad("headerId", header.getHeaderId())
                    .build());
        } else {
            BcqEventCode code = header.getStatus() == CONFIRMED ? NTF_BCQ_CONFIRM_SELLER : NTF_BCQ_NULLIFY_SELLER;
            notificationService.notify(new NotificationBuilder()
                    .withCode(code.toString())
                    .withRecipientId(header.getSellingParticipantUserId())
                    .addLoad("tradingDate", formattedTradingDate)
                    .addLoad("respondedDate", formattedRespondedDate)
                    .addLoad("buyerName", header.getBuyingParticipantName())
                    .addLoad("buyerShortName", header.getBuyingParticipantShortName())
                    .addLoad("headerId", header.getHeaderId())
                    .build());
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
        notificationService.notify(new NotificationBuilder()
                .withCode(sellerCode.toString())
                .withRecipientId(firstHeader.getSellingParticipantUserId())
                .addLoad("tradingDate", formattedTradingDate)
                .addLoad("sellingMtns", sellingMtns.toString())
                .addLoad("buyerName", firstHeader.getBuyingParticipantName())
                .addLoad("buyerShortName", firstHeader.getBuyingParticipantShortName())
                .addLoad("deadlineDate", formattedDeadlineDate)
                .addLoad("status", status)
                .build());
        notificationService.notify(new NotificationBuilder()
                .withCode(buyerCode.toString())
                .withRecipientId(firstHeader.getBuyingParticipantUserId())
                .addLoad("tradingDate", formattedTradingDate)
                .addLoad("sellingMtns", sellingMtns.toString())
                .addLoad("sellerName", firstHeader.getSellingParticipantName())
                .addLoad("sellerShortName", firstHeader.getSellingParticipantShortName())
                .addLoad("deadlineDate", formattedDeadlineDate)
                .addLoad("status", status)
                .build());
    }

    //TODO Find a better approach for this
    @SuppressWarnings("unchecked")
    private String getSettlementName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LinkedHashMap<String, Object> oAuthPrincipal = (LinkedHashMap) auth.getPrincipal();
        LinkedHashMap<String, Object> userAuthentication = (LinkedHashMap) oAuthPrincipal.get("userAuthentication");
        LinkedHashMap<String, String> principal = (LinkedHashMap) userAuthentication.get("principal");
        return principal.get("firstName") + " "
                + principal.get("lastName") + " ("
                + principal.get("username") + ")";
    }

}

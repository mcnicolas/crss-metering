package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.BcqHeader;

import java.util.Date;
import java.util.List;

public interface BcqNotificationManager {

    void sendValidationNotification(BcqDeclaration declaration, Date submittedDate);

    void sendUploadNotification(List<BcqHeader> headerList);

    void sendSettlementUploadNotification(List<BcqHeader> headerList);

    void sendUpdateStatusNotification(BcqHeader header);

    void sendUnprocessedNotification(List<BcqHeader> headerList, BcqStatus status);

}
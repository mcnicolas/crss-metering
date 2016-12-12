package com.pemc.crss.metering.dto;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import com.pemc.crss.metering.constants.BcqStatus;

import java.util.Date;

public class BcqHeaderDisplay extends AbstractWebDto<BcqHeader> {

    public BcqHeaderDisplay(BcqHeader target) {
        super(target);
    }

    public long getHeaderId() {
        return target().getHeaderId();
    }

    public String getBillingId() {
        return target().getBillingId();
    }

    public String getSellingParticipantName() {
        return target().getSellingParticipantName();
    }

    public String getBuyingParticipantName() {
        return target().getBuyingParticipantName();
    }

    public String getBuyingParticipantShortName() {
        return target().getBuyingParticipantShortName();
    }

    public String getSellingParticipantShortName() {
        return target().getSellingParticipantShortName();
    }

    public String getSellingMtn() {
        return target().getSellingMtn();
    }

    public Date getTradingDate() {
        return target().getTradingDate();
    }

    public Date getDeadlineDate() {
        return target().getDeadlineDate();
    }

    public String getTransactionId() {
        return target().getUploadFile().getTransactionId();
    }

    public Date getSubmittedDate() {
        return target().getUploadFile().getSubmittedDate();
    }

    public String getUpdatedVia() {
        return target().getUpdatedVia();
    }

    public BcqStatus getStatus() {
        return target().getStatus();
    }
}

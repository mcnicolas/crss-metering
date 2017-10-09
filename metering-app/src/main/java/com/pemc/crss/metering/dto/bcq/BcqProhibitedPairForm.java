package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import com.pemc.crss.metering.utils.DateTimeUtils;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@ToString
public class BcqProhibitedPairForm extends AbstractWebDto<BcqProhibitedPair> {

    public BcqProhibitedPairForm() {
        super(new BcqProhibitedPair());
    }

    public BcqProhibitedPairForm(BcqProhibitedPair target) {
        super(target);
    }

    public String getSellingMtn() {
        return target().getSellingMtn();
    }

    public void setSellingMtn(String sellingMtn) {
        target().setSellingMtn(sellingMtn);
    }

    public String getBillingId() {
        return target().getBillingId();
    }

    public void setBillingId(String billingId) {
        target().setBillingId(billingId);
    }

    public String getCreatedBy() {
        return target().getCreatedBy();
    }

    public void setCreatedBy(String createdBy) {
        target().setCreatedBy(createdBy);
    }

    public LocalDateTime getEffectiveStartDate() {
        return  target().getEffectiveStartDate();
    }

    public void setEffectiveStartDate(String effectiveStartDate) {
        target().setEffectiveStartDate(DateTimeUtils.parseDateTime24hr(effectiveStartDate));
    }

    public LocalDateTime getEffectiveEndDate() {
        return  target().getEffectiveEndDate();
    }

    public void setEffectiveEndDate(String effectiveEndDate) {
        target().setEffectiveEndDate(DateTimeUtils.parseDateTime24hr(effectiveEndDate));
    }
}

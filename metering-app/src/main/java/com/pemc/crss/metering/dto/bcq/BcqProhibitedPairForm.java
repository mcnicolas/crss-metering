package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import lombok.ToString;

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

}

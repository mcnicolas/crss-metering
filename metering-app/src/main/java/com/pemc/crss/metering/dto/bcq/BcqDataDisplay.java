package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;

import java.math.BigDecimal;
import java.util.Date;

public class BcqDataDisplay extends AbstractWebDto<BcqData> {

    public BcqDataDisplay() {
        super(new BcqData());
    }

    public BcqDataDisplay(BcqData target) {
        super(target);
    }

    public String getReferenceMtn() {
        return target().getReferenceMtn().toUpperCase();
    }

    public void setReferenceMtn(String referenceMtn) {
        target().setReferenceMtn(referenceMtn);
    }

    public Date getStartTime() {
        return target().getStartTime();
    }

    public void setStartTime(Date startTime) {
        target().setStartTime(startTime);
    }

    public Date getEndTime() {
        return target().getEndTime();
    }

    public void setEndTime(Date endTime) {
        target().setEndTime(endTime);
    }

    public String getBcq() {
        return target().getBcq().toPlainString();
    }

    public void setBcq(String bcq) {
        target().setBcq(new BigDecimal(bcq));
    }
}

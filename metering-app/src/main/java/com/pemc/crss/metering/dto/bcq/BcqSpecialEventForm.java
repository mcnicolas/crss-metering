package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;

import java.util.Date;
import java.util.List;

public class BcqSpecialEventForm extends AbstractWebDto<BcqSpecialEvent> {

    public BcqSpecialEventForm() {
        super(new BcqSpecialEvent());
    }

    public BcqSpecialEventForm(BcqSpecialEvent specialEvent) {
        super(specialEvent);
    }

    public List<Date> getTradingDates() {
        return target().getTradingDates();
    }

    public void setTradingDates(List<Date> tradingDates) {
        target().setTradingDates(tradingDates);
    }

    public Date getDeadlineDate() {
        return target().getDeadlineDate();
    }

    public void setDeadlineDate(Date deadlineDate) {
        target().setDeadlineDate(deadlineDate);
    }

    public List<String> getTradingParticipants() {
        return target().getTradingParticipants();
    }

    public void setTradingParticipants(List<String> tradingParticipants) {
        target().setTradingParticipants(tradingParticipants);
    }

    public String getRemarks() {
        return target().getRemarks();
    }

    public void setRemarks(String remarks) {
        target().setRemarks(remarks);
    }

}

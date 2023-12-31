package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.BcqStatus.fromString;

@ToString
public class BcqHeaderDetails extends AbstractWebDto<BcqHeader> {

    public BcqHeaderDetails() {
        super(new BcqHeader());
    }

    public BcqHeaderDetails(BcqHeader target) {
        super(target);
    }

    public String getBillingId() {
        return target().getBillingId().toUpperCase();
    }

    public void setBillingId(String billingId) {
        target().setBillingId(billingId);
    }

    public String getSellingMtn() {
        return target().getSellingMtn().toUpperCase();
    }

    public void setSellingMtn(String sellingMtn) {
        target().setSellingMtn(sellingMtn);
    }

    public String getBuyingParticipantName() {
        return target().getBuyingParticipantName();
    }

    public void setBuyingParticipantName(String buyingParticipantName) {
        target().setBuyingParticipantName(buyingParticipantName);
    }

    public String getBuyingParticipantShortName() {
        return target().getBuyingParticipantShortName();
    }

    public void setBuyingParticipantShortName(String buyingParticipantShortName) {
        target().setBuyingParticipantShortName(buyingParticipantShortName);
    }

    public String getStatus() {
        return target().getStatus() != null ? target().getStatus().toString() : null;
    }

    public void setStatus(String status) {
        if (status != null) {
            target().setStatus(fromString(status));
        }
    }

    public Date getTradingDate() {
        return target().getTradingDate();
    }

    public void setTradingDate(Date tradingDate) {
        target().setTradingDate(tradingDate);
    }

    public List<BcqDataDetails> getDataDetailsList() {
        return target().getDataList()
                .stream()
                .map(BcqDataDetails::new)
                .collect(Collectors.toList());
    }

    public void setDataDetailsList(List<BcqDataDetails> dataInfoList) {
        List<BcqData> dataList = new ArrayList<>();
        dataInfoList.forEach(dataInfo -> dataList.add(dataInfo.target()));
        target().setDataList(dataList);
    }

}

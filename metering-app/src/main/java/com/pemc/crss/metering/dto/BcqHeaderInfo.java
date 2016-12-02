package com.pemc.crss.metering.dto;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import com.pemc.crss.metering.constants.BcqStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BcqHeaderInfo extends AbstractWebDto<BcqHeader> {

    public BcqHeaderInfo() {
        super(new BcqHeader());
    }

    public BcqHeaderInfo(BcqHeader target) {
        super(target);
    }

    public String getSellingMtn() {
        return target().getSellingMtn();
    }

    public void setSellingMtn(String sellingMtn) {
        target().setSellingMtn(sellingMtn);
    }

    public String getBuyingParticipant() {
        return target().getBuyingParticipant();
    }

    public void setBuyingParticipant(String buyingParticipant) {
        target().setBuyingParticipant(buyingParticipant);
    }

    public String getSellingParticipantName() {
        return target().getSellingParticipantName();
    }

    public void setSellingParticipantName(String sellingParticipantName) {
        target().setSellingParticipantName(sellingParticipantName);
    }

    public String getSellingParticipantShortName() {
        return target().getSellingParticipantShortName();
    }

    public void setSellingParticipantShortName(String sellingParticipantShortName) {
        target().setSellingParticipantShortName(sellingParticipantShortName);
    }

    public BcqStatus getStatus() {
        return target().getStatus();
    }

    public void setStatus(String status) {
        target().setStatus(BcqStatus.fromString(status));
    }

    public Date getTradingDate() {
        return target().getTradingDate();
    }

    public void setTradingDate(Date tradingDate) {
        target().setTradingDate(tradingDate);
    }

    public List<BcqDataInfo> getDataInfoList() {
        return target().getDataList()
                .stream()
                .map(BcqDataInfo::new)
                .collect(Collectors.toList());
    }

    public void setDataInfoList(List<BcqDataInfo> dataInfoList) {
        List<BcqData> dataList = new ArrayList<>();
        dataInfoList.forEach(dataInfo -> {
            dataList.add(dataInfo.target());
        });
        target().setDataList(dataList);
    }

}

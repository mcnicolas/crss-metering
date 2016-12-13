package com.pemc.crss.metering.dto;

import com.pemc.crss.commons.web.dto.AbstractWebDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BcqDetailsInfo extends AbstractWebDto<BcqDetails> {

    private boolean recordExists;

    public BcqDetailsInfo() {
        super(new BcqDetails());
    }

    public BcqDetailsInfo(BcqDetails target) {
        super(target);
    }

    public BcqUploadFileInfo getFileInfo() {
        return new BcqUploadFileInfo(target().getFile());
    }

    public void setFileInfo(BcqUploadFileInfo fileInfo) {
        target().setFile(fileInfo.target());
    }

    public List<BcqHeaderInfo> getHeaderInfoList() {
        return target().getHeaderList()
                .stream()
                .map(BcqHeaderInfo::new)
                .collect(Collectors.toList());
    }

    public void setHeaderInfoList(List<BcqHeaderInfo> headerInfoList) {
        List<BcqHeader> headerList = new ArrayList<>();
        headerInfoList.forEach(headerInfo -> headerList.add(headerInfo.target()));
        target().setHeaderList(headerList);
    }

    public List<Long> getBuyerIds() {
        return target().getBuyerIds();
    }

    public void setBuyerIds(List<Long> buyerIds) {
        target().setBuyerIds(buyerIds);
    }

    public Long getSellerId() {
        return target().getSellerId();
    }

    public void setSellerId(Long sellerId) {
        target().setSellerId(sellerId);
    }

    public boolean isRecordExists() {
        return recordExists;
    }

    public void setRecordExists(boolean recordExists) {
        this.recordExists = recordExists;
    }
}

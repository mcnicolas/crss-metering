package com.pemc.crss.metering.dto.bcq;

import lombok.Data;

import java.util.List;

@Data
public class BcqDownloadDto {
    private List<String> buyerBillingIds;
    private List<String> sellingMtns;
    private String date;
    private String genName;
    private String errorMsg;
}

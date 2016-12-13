package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BcqDetails {

    private Long sellerId;
    private String sellerName;
    private String sellerShortName;
    private BcqUploadFile file;
    private List<BcqHeader> headerList;
    private List<Long> buyerIds;
    private String errorMessage;
    private boolean recordExists;

}

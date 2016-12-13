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
    private BcqUploadFile file;
    private List<BcqHeader> headerList;
    private List<Long> buyerIds;

}

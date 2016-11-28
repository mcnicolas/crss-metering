package com.pemc.crss.metering.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BcqDataInfo {

    private String referenceMtn;
    private Date startTime;
    private Date endTime;
    private String bcq;

}

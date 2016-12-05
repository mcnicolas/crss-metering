package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.util.List;

@Data
public class MeterData {

    private MeterDataHeader header;
    private List<MeterDataDetail> details;

}

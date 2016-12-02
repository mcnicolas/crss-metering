package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.util.List;

@Data
public class MeterDataHeader {

    private List<String> columnNames;

}

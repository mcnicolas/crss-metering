package com.pemc.crss.metering.resource.bcq_data.extraction.dto;

import lombok.Data;

import java.util.List;

@Data
public class BcqHeaderDto {
    private final String tradingparticipant;
    private final String tradingDate;
}

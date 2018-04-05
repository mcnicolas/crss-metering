package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MqExtractionMonthlyHeader extends MqExtractionHeader {


    @JsonProperty("trading_date")
    private String tradingDate;

    public MqExtractionMonthlyHeader(String mspShortName, String sein, String tradingDate) {
        super("MONTHLY", mspShortName, sein);
        this.tradingDate = tradingDate;
    }
}

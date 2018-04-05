package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Deprecated
@Data
@JsonPropertyOrder({"meterdata_category", "sein", "msp_shortname", "trading_date", "meter_data"})
public class MqExtractionDailyHeader {

    @JsonProperty("trading_date")
    private String tradingDate;

}

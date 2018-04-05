package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Deprecated
@Data
@JsonPropertyOrder({"meterdata_category", "sein", "msp_shortname", "billing_period_start", "billing_period_end", "meter_data"})
public class MqExtractionMonthlyHeader {


    @JsonProperty("billing_period_start")
    private String billingPeriodStart;

    @JsonProperty("billing_period_end")
    private String billingPeriodEnd;

}

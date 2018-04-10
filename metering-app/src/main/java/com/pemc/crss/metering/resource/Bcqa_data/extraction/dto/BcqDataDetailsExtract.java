package com.pemc.crss.metering.resource.Bcqa_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonPropertyOrder({"reference_mtn", "dispatch_interval", "bcq"})
public class BcqDataDetailsExtract {
    @JsonProperty("reference_mtn")
    private final String referenceMtn;
    @JsonProperty("dispatch_interval")
    private final Long dispatchInterval;
    @JsonProperty("bcq")
    private final BigDecimal bcq;

    public BcqDataDetailsExtract(String referenceMtn, Long dispatchInterval, BigDecimal bcq) {
        this.referenceMtn = referenceMtn;
        this.dispatchInterval = dispatchInterval;
        this.bcq = bcq;
    }
}

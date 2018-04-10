package com.pemc.crss.metering.resource.bcq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonPropertyOrder({"value"})
public class BcqDataDetailsExtract {
    @JsonProperty("value")
    private final String value;

    }

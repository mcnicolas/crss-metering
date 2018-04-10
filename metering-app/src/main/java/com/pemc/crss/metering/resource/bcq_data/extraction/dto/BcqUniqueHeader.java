package com.pemc.crss.metering.resource.bcq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.Set;

@Data
@JsonPropertyOrder({"trading_participant", "trading_date", "columns", "bcq_data"})
public class BcqUniqueHeader {
    @JsonProperty("trading_participant")
   private final String tradingparticipant;

    @JsonProperty("trading_date")
    private final String tradingDate;


    @JsonProperty("columns")
    private final String columns = "dispatch_interval, bcq, reference_mtn, buyer_mtn";

    @JsonProperty("bcq_data")
    private final Set<BcqDataHeader> bcq_data;
}


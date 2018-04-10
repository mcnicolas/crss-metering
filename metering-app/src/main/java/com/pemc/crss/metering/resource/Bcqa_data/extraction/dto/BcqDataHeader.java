package com.pemc.crss.metering.resource.Bcqa_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"version", "buying_participant", "selling_mtn", "submitted_date",
        "deadline_date", "status", "transaction_id", "billing_id", "trading_date", "updated_via", "bcq_details"})
public class BcqDataHeader {
    @JsonProperty("version")
    private final String version;

    @JsonProperty("buying_participant")
    private final String buyingParticipant;

    @JsonProperty("selling_mtn")
    private final String sellingMtn;
    @JsonProperty("submitted_date")
    private final String submittedDate;

    @JsonProperty("deadline_date")
    private final String deadlineDate;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("transaction_id")
    private final String transactionId;

    @JsonProperty("billing_id")
    private final String billingId;

    @JsonProperty("trading_date")
    private final String tradingDate;

    @JsonProperty("updated_via")
    private final String updatedVia;

    @JsonProperty("bcq_details")
    private final List<BcqDataDetailsExtract> bcq_details;

    public BcqDataHeader(String version, String buyingParticipant, String sellingMtn,
                         String submittedDate, String deadlineDate, String status,
                         String transactionId, String billingId, String tradingDate,
                         String updatedVia, List<BcqDataDetailsExtract> bcq_details) {
        this.version = version;
        this.buyingParticipant = buyingParticipant;
        this.sellingMtn = sellingMtn;
        this.submittedDate = submittedDate;
        this.deadlineDate = deadlineDate;
        this.status = status;
        this.transactionId = transactionId;
        this.billingId = billingId;
        this.tradingDate = tradingDate;
        this.updatedVia = updatedVia;
        this.bcq_details = bcq_details;
    }
}

package com.pemc.crss.metering.resource.bcq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"transaction_id", "version", "selling_mtn", "billing_id", "buying_participant",
        "submitted_date", "deadline_date", "status", "updated_via", "record_count", "bcq_detail"})
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

    @JsonProperty("updated_via")
    private final String updatedVia;

    @JsonProperty("record_count")
    private final int recordCount;


    @JsonProperty("bcq_detail")
    private final List<BcqDataDetailsExtract> bcq_details;

    public BcqDataHeader(String version, String buyingParticipant, String sellingMtn,
                         String submittedDate, String deadlineDate, String status,
                         String transactionId, String billingId,
                         String updatedVia, int recordCount,
                         List<BcqDataDetailsExtract> bcq_details) {
        this.version = version;
        this.buyingParticipant = buyingParticipant;
        this.sellingMtn = sellingMtn;
        this.submittedDate = submittedDate;
        this.deadlineDate = deadlineDate;
        this.status = status;
        this.transactionId = transactionId;
        this.billingId = billingId;
        this.updatedVia = updatedVia;
        this.bcq_details = bcq_details;
        this.recordCount = recordCount;
    }
}

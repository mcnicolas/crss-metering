package com.pemc.crss.metering.resource.bcq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"transaction_id", "seller_billing_id", "buyer_billing_id",
        "submitted_date", "status", "uploaded_via", "record_count", "bcq_details"})
public class BcqDataHeader {


    @JsonProperty("seller_billing_id")
    private final String sellerBillingId;

    @JsonProperty("submitted_date")
    private final String submittedDate;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("transaction_id")
    private final String transactionId;

    @JsonProperty("buyer_billing_id")
    private final String buyer_BillingId;

    @JsonProperty("uploaded_via")
    private final String updatedVia;

    @JsonProperty("record_count")
    private final int recordCount;


    @JsonProperty("bcq_details")
    private final List<BcqDataDetailsExtract> bcq_details;

    public BcqDataHeader(String sellerBillingId, String submittedDate,  String status,
                         String transactionId, String buyer_BillingId, String updatedVia, int recordCount,
                         List<BcqDataDetailsExtract> bcq_details) {
        this.sellerBillingId = sellerBillingId;
        this.submittedDate = submittedDate;
        this.status = status;
        this.transactionId = transactionId;
        this.buyer_BillingId = buyer_BillingId;
        this.updatedVia = updatedVia;
        this.recordCount = recordCount;
        this.bcq_details = bcq_details;
    }
}

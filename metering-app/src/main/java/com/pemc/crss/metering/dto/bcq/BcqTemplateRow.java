package com.pemc.crss.metering.dto.bcq;

import lombok.Data;

@Data
public class BcqTemplateRow {

    private String sellingMtn;
    private String buyerBillingId;
    private String date;
    private String refMtn;
    private String bcq;
    private String buyerMtn;

    public BcqTemplateRow(String sellingMtn, String buyerBillingId, String date) {
        this.sellingMtn = sellingMtn;
        this.buyerBillingId = buyerBillingId;
        this.date = date;
    }
}

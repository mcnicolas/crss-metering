package com.pemc.crss.metering.dto.bcq;

import lombok.Data;

import java.util.Date;

@Data
public class BcqProhibitedPairPageDisplay {

    private long id;
    private String sellingMtn;
    private String billingId;
    private String createdBy;
    private Date createdDate;

    public String getSellingMtn() {
        return sellingMtn.toUpperCase();
    }

    public String getBillingId() {
        return billingId.toUpperCase();
    }

}

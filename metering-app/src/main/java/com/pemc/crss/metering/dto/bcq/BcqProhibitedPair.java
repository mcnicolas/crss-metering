package com.pemc.crss.metering.dto.bcq;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@ToString
public class BcqProhibitedPair {

    private long id;
    private String sellingMtn;
    private String billingId;
    private String createdBy;
    private Date createdDate;
    private LocalDateTime effectiveStartDate;
    private LocalDateTime effectiveEndDate;
    private boolean enabled;

}

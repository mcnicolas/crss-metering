package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.constants.BcqStatus;
import lombok.Data;

import java.util.Date;

@Data
public class BcqUpdateStatusDetails {

    private BcqStatus status;
    private Long buyerId;
    private String buyerName;
    private String buyerShortName;
    private Long sellerId;
    private String sellerName;
    private String sellerShortName;
    private Date tradingDate;

}

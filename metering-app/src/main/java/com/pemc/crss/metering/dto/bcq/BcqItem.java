package com.pemc.crss.metering.dto.bcq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class BcqItem {

    private String sellingMtn;
    private  List<String>  buyerMtns;
    private String tradingParticipantShortName;
    private List<String> referenceMtns;
    private Date tradingDate;
    private String  billingId;

    public BcqItem(String tradingParticipantShortName, Date tradingDate) {
        this.tradingParticipantShortName = tradingParticipantShortName;
        this.tradingDate = tradingDate;
    }
}

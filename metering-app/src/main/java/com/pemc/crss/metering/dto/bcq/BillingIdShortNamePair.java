package com.pemc.crss.metering.dto.bcq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingIdShortNamePair {

    private String billingId;
    private List<String> tradingParticipantShortName;

}

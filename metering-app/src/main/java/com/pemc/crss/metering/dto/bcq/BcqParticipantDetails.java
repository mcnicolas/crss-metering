package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class BcqParticipantDetails {

    private List<ParticipantBuyerDetails> buyerDetailsList;
    private BcqValidationResult validationResult;

}

package com.pemc.crss.metering.validator.bcq.handler;

import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;

import java.util.Date;
import java.util.List;

public interface BcqValidationHandler {

    BcqDeclaration processAndValidate(List<List<String>> csv);
    BcqDeclaration processAndValidateForSettlement(List<List<String>> csv, ParticipantSellerDetails sellerDetails,
                                                   Date tradingDate);

}

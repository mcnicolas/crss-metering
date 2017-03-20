package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;

import java.util.Date;

public interface OverrideValidationHelper {

    HeaderListValidation validOverride(String sellingParticipant, Date tradingDate);

}

package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.validator.bcq.validation.BillingIdValidation;

import java.util.Date;

public interface BillingIdValidationHelper {

    BillingIdValidation validBillingIds(Date tradingDate);

}

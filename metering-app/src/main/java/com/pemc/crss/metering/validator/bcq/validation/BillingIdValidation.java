package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BillingIdShortNamePair;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;

import java.util.List;
import java.util.function.Predicate;

public class BillingIdValidation extends AbstractValidation<List<BillingIdShortNamePair>> {

    public BillingIdValidation() {
        super();
    }

    public BillingIdValidation(Predicate<List<BillingIdShortNamePair>> predicate, BcqValidationErrorMessage errorMessage) {
        super(predicate, errorMessage);
    }

}

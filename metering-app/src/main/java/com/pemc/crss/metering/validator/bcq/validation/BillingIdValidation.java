package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;

import java.util.List;
import java.util.function.Predicate;

public class BillingIdValidation extends AbstractValidation<List<String>> {

    public BillingIdValidation() {
        super();
    }

    public BillingIdValidation(Predicate<List<String>> predicate, BcqValidationErrorMessage errorMessage) {
        super(predicate, errorMessage);
    }

}

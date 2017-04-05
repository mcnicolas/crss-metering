package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;

import java.util.List;
import java.util.function.Predicate;

public class ProhibitedValidation  extends AbstractValidation<List<BcqHeader>, List<BcqHeader>> {

    public ProhibitedValidation() {
        super();
    }

    public ProhibitedValidation(Predicate<List<BcqHeader>> predicate, BcqValidationErrorMessage errorMessage) {
        super(predicate, errorMessage);
    }

}

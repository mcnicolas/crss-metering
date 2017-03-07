package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;

import java.util.List;
import java.util.function.Predicate;

public class HeaderListValidation extends AbstractValidation<List<BcqHeader>> {

    public HeaderListValidation() {
        super();
    }

    public HeaderListValidation(Predicate<List<BcqHeader>> predicate, BcqValidationErrorMessage errorMessage) {
        super(predicate, errorMessage);
    }

}

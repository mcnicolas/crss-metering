package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqItem;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;

import java.util.List;
import java.util.function.Predicate;

public class CrssSideValidation extends AbstractValidation<List<BcqItem>, List<BcqHeader>> {

    public CrssSideValidation() {
        super();
    }

    public CrssSideValidation(Predicate<List<BcqItem>> predicate, BcqValidationErrorMessage errorMessage) {
        super(predicate, errorMessage);
    }

}

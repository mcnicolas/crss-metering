package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;

import java.util.List;
import java.util.function.Predicate;

public class CsvValidation extends AbstractValidation<List<List<String>>, List<BcqHeader>> {

    public CsvValidation() {
        super();
    }

    public CsvValidation(Predicate<List<List<String>>> predicate, BcqValidationErrorMessage errorMessage) {
        super(predicate, errorMessage);
    }

}

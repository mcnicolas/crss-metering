package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class OverrideValidation implements Validation<List<BcqHeader>> {

    private Predicate<List<BcqHeader>> predicate;
    private String errorMessage;

    private OverrideValidation(Predicate<List<BcqHeader>> predicate, String errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<BcqHeader> headerList) {
        return predicate.test(headerList) ? accepted() : rejected(errorMessage);
    }

    public static OverrideValidation emptyInst() {
        return new OverrideValidation(null, null);
    }

    public static OverrideValidation from(Predicate<List<BcqHeader>> predicate) {
        return new OverrideValidation(predicate, null);
    }

    public static OverrideValidation from(Predicate<List<BcqHeader>> predicate, String onErrorMessage) {
        return new OverrideValidation(predicate, onErrorMessage);
    }

    public void setPredicate(Predicate<List<BcqHeader>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

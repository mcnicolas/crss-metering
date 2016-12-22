package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class HeaderListValidation implements Validation<List<BcqHeader>> {

    private Predicate<List<BcqHeader>> predicate;
    private String errorMessage;

    private HeaderListValidation(Predicate<List<BcqHeader>> predicate, String errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<BcqHeader> csvLines) {
        return predicate.test(csvLines) ? accepted() : rejected(errorMessage);
    }

    public static HeaderListValidation emptyInst() {
        return new HeaderListValidation(null, null);
    }

    public static HeaderListValidation from(Predicate<List<BcqHeader>> predicate) {
        return new HeaderListValidation(predicate, null);
    }

    public static HeaderListValidation from(Predicate<List<BcqHeader>> predicate, String onErrorMessage) {
        return new HeaderListValidation(predicate, onErrorMessage);
    }

    public void setPredicate(Predicate<List<BcqHeader>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
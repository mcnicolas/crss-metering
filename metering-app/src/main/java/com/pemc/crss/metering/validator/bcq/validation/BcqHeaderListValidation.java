package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class BcqHeaderListValidation implements BcqValidation<List<BcqHeader>> {

    private Predicate<List<BcqHeader>> predicate;
    private String errorMessage;

    private BcqHeaderListValidation(Predicate<List<BcqHeader>> predicate, String errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<BcqHeader> csvLines) {
        return predicate.test(csvLines) ? accepted() : rejected(errorMessage);
    }

    public static BcqHeaderListValidation emptyInst() {
        return new BcqHeaderListValidation(null, null);
    }

    public static BcqHeaderListValidation from(Predicate<List<BcqHeader>> predicate) {
        return new BcqHeaderListValidation(predicate, null);
    }

    public static BcqHeaderListValidation from(Predicate<List<BcqHeader>> predicate, String onErrorMessage) {
        return new BcqHeaderListValidation(predicate, onErrorMessage);
    }

    public void setPredicate(Predicate<List<BcqHeader>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

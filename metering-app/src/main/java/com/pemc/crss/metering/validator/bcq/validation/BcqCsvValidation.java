package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class BcqCsvValidation implements BcqValidation<List<List<String>>> {

    private Predicate<List<List<String>>> predicate;
    private String errorMessage;

    private BcqCsvValidation(Predicate<List<List<String>>> predicate, String errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<List<String>> csvLines) {
        return predicate.test(csvLines) ? accepted() : rejected(errorMessage);
    }

    public static BcqCsvValidation emptyInst() {
        return new BcqCsvValidation(null, null);
    }

    public static BcqCsvValidation from(Predicate<List<List<String>>> predicate) {
        return new BcqCsvValidation(predicate, null);
    }

    public static BcqCsvValidation from(Predicate<List<List<String>>> predicate, String onErrorMessage) {
        return new BcqCsvValidation(predicate, onErrorMessage);
    }

    public void setPredicate(Predicate<List<List<String>>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

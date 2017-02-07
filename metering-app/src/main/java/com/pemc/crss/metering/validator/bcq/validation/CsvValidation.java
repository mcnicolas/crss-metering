package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class CsvValidation implements Validation<List<List<String>>> {

    private Predicate<List<List<String>>> predicate;
    private BcqValidationErrorMessage errorMessage;

    private CsvValidation(Predicate<List<List<String>>> predicate, BcqValidationErrorMessage errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<List<String>> csvLines) {
        return predicate.test(csvLines) ? accepted() : rejected(errorMessage);
    }

    public static CsvValidation emptyInst() {
        return new CsvValidation(null, null);
    }

    public static CsvValidation from(Predicate<List<List<String>>> predicate) {
        return new CsvValidation(predicate, null);
    }

    public static CsvValidation from(Predicate<List<List<String>>> predicate, BcqValidationErrorMessage errorMessage) {
        return new CsvValidation(predicate, errorMessage);
    }

    public void setPredicate(Predicate<List<List<String>>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(BcqValidationErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

}

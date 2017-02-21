package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class BillingIdValidation implements Validation<List<String>> {

    private Predicate<List<String>> predicate;
    private BcqValidationErrorMessage errorMessage;

    private BillingIdValidation(Predicate<List<String>> predicate, BcqValidationErrorMessage errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<String> csvLines) {
        return predicate.test(csvLines) ? accepted() : rejected(errorMessage);
    }

    public static BillingIdValidation emptyInst() {
        return new BillingIdValidation(null, null);
    }

    public static BillingIdValidation from(Predicate<List<String>> predicate) {
        return new BillingIdValidation(predicate, null);
    }

    public static BillingIdValidation from(Predicate<List<String>> predicate,
                                            BcqValidationErrorMessage errorMessage) {

        return new BillingIdValidation(predicate, errorMessage);
    }

    public void setPredicate(Predicate<List<String>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(BcqValidationErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

}

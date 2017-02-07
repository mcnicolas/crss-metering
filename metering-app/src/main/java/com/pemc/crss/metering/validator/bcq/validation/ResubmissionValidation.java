package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class ResubmissionValidation implements Validation<List<BcqHeader>> {

    private Predicate<List<BcqHeader>> predicate;
    private BcqValidationErrorMessage errorMessage;

    private ResubmissionValidation(Predicate<List<BcqHeader>> predicate, BcqValidationErrorMessage errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    @Override
    public BcqValidationResult test(List<BcqHeader> headerList) {
        return predicate.test(headerList) ? accepted() : rejected(errorMessage);
    }

    public static ResubmissionValidation emptyInst() {
        return new ResubmissionValidation(null, null);
    }

    public static ResubmissionValidation from(Predicate<List<BcqHeader>> predicate) {
        return new ResubmissionValidation(predicate, null);
    }

    public static ResubmissionValidation from(Predicate<List<BcqHeader>> predicate,
                                              BcqValidationErrorMessage errorMessage) {

        return new ResubmissionValidation(predicate, errorMessage);
    }

    public void setPredicate(Predicate<List<BcqHeader>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(BcqValidationErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

}

package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

public class HeaderListValidation implements Validation<List<BcqHeader>> {

    private Predicate<List<BcqHeader>> predicate;
    private BcqValidationErrorMessage errorMessage;

    private HeaderListValidation(Predicate<List<BcqHeader>> predicate, BcqValidationErrorMessage errorMessage) {
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

    public static HeaderListValidation from(Predicate<List<BcqHeader>> predicate,
                                            BcqValidationErrorMessage errorMessage) {

        return new HeaderListValidation(predicate, errorMessage);
    }

    public void setPredicate(Predicate<List<BcqHeader>> predicate) {
        this.predicate = predicate;
    }

    public void setErrorMessage(BcqValidationErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

}

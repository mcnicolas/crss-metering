package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;
import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.rejected;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractValidation<T> implements Validation<T> {

    protected Predicate<T> predicate;
    protected BcqValidationErrorMessage errorMessage;

    @Override
    public BcqValidationResult test(T objectToValidate) {
        return predicate.test(objectToValidate) ? accepted() : rejected(errorMessage);
    }

}

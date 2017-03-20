package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractValidation<T, R> implements Validation<T> {

    protected Predicate<T> predicate;
    protected BcqValidationErrorMessage errorMessage;

    @Override
    public BcqValidationResult<R> test(T objectToValidate) {
        return predicate.test(objectToValidate) ? new BcqValidationResult<>() : new BcqValidationResult<>(errorMessage);
    }

}

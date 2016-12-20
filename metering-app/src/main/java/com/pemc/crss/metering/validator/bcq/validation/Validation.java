package com.pemc.crss.metering.validator.bcq.validation;

import com.pemc.crss.metering.validator.bcq.BcqValidationResult;

import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@FunctionalInterface
public interface Validation<T> {

    BcqValidationResult test(T object);

    default Validation<T> and(Validation<T> other) {
        return (param) -> {
            BcqValidationResult firstResult = this.test(param);
            return firstResult.getStatus() == REJECTED ? firstResult : other.test(param);
        };
    }

}

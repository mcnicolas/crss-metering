package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.constants.ValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

import java.util.function.Function;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static lombok.AccessLevel.PRIVATE;

@Data
@AllArgsConstructor(access = PRIVATE)
@Wither
public class BcqValidationResult<T> {

    private ValidationStatus status;
    private BcqValidationErrorMessage errorMessage;
    private T processedObject;

    public BcqValidationResult() {
        this(ACCEPTED, null, null);
    }

    public BcqValidationResult(BcqValidationErrorMessage errorMessage) {
        this(REJECTED, errorMessage, null);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("status", status)
                .add("errorMessage", errorMessage)
                .toString();
    }

    public BcqValidationResult<T> then(Function<BcqValidationResult<T>, BcqValidationResult<T>> function) {
        if (status == REJECTED) {
            return this;
        }
        return function.apply(this);
    }

}

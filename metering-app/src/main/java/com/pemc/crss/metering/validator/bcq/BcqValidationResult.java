package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.constants.ValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class BcqValidationResult {

    private ValidationStatus status;
    private BcqValidationErrorMessage errorMessage;

    public static BcqValidationResult accepted() {
        return new BcqValidationResult(ACCEPTED, null);
    }

    public static BcqValidationResult rejected(BcqValidationErrorMessage errorMessage) {
        return new BcqValidationResult(REJECTED, errorMessage);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("status", status)
                .add("errorMessage", errorMessage)
                .toString();
    }

}

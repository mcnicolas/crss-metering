package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.constants.ValidationStatus;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

public class BcqValidationResult {

    private ValidationStatus status;
    private String errorMessage;

    private BcqValidationResult(ValidationStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ValidationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static BcqValidationResult accepted() {
        return new BcqValidationResult(ACCEPTED, null);
    }

    public static BcqValidationResult rejected(String errorMessage) {
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

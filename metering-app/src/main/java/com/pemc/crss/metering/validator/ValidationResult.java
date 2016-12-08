package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.constants.ValidationStatus;
import lombok.Data;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Data
public class ValidationResult {

    public static final ValidationResult ACCEPTED_STATUS = new ValidationResult(ACCEPTED);
    public static final ValidationResult REJECTED_STATUS = new ValidationResult(REJECTED);

    private long fileID;
    private ValidationStatus status;
    private String errorDetail = "";

    public ValidationResult() {
    }

    public ValidationResult(ValidationStatus status) {
        this.status = status;
    }

}
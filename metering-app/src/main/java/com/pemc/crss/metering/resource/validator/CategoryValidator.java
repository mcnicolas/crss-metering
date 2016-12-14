package com.pemc.crss.metering.resource.validator;

import com.pemc.crss.metering.constants.UploadType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class CategoryValidator implements ConstraintValidator<ValidCategory, String> {

    @Override
    public void initialize(ValidCategory constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        for (UploadType uploadType : UploadType.values()) {
            if (equalsIgnoreCase(uploadType.toString(), value)) {
                return true;
            }
        }

        return false;
    }

}

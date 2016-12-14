package com.pemc.crss.metering.resource.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MSPValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        // TODO: Additional validation
        // 1. Check if msp is an existing entity from registration
        // 2. Check if msp is valid for the currently logged in user
//        private String mspShortName;
    }

}

package com.pemc.crss.metering.resource.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class HeaderIDValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        // TODO: Additional validaton
        // 1. Check if headerID is existing in the header manifest table
        // 2. Check if the record does not have a trailer record yet
//        private Long headerID;
    }

}

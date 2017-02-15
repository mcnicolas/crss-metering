package com.pemc.crss.metering.resource.validator;

import com.pemc.crss.metering.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class MSPValidator implements ConstraintValidator<ValidMSP, String> {

    private final CacheService cacheService;

    @Autowired
    public MSPValidator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void initialize(ValidMSP constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return cacheService.getParticipantUserDetail(value) != null;
    }

}

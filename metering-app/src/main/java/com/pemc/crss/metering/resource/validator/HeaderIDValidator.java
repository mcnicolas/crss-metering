package com.pemc.crss.metering.resource.validator;

import com.pemc.crss.metering.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class HeaderIDValidator implements ConstraintValidator<ExistingHeaderID, Long> {

    private final MeterService meterService;

    @Autowired
    public HeaderIDValidator(MeterService meterService) {
        this.meterService = meterService;
    }

    @Override
    public void initialize(ExistingHeaderID constraintAnnotation) {
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        return meterService.isHeaderValid(value);
    }

}

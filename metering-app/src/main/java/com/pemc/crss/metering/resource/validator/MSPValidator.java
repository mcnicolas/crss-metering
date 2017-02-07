package com.pemc.crss.metering.resource.validator;

import com.pemc.crss.metering.resource.template.ResourceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
public class MSPValidator implements ConstraintValidator<ValidMSP, String> {

    private static final String MSP_LISTING_URL = "/reg/participants/category/msp";

    private final ResourceTemplate resourceTemplate;

    @Autowired
    public MSPValidator(ResourceTemplate resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
    }

    @Override
    public void initialize(ValidMSP constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean retVal = false;

        Class<List<Map>> clazz = (Class) List.class;
        List<Map> mspNames = resourceTemplate.get(MSP_LISTING_URL, clazz);

        for (Map mspName : mspNames) {
            if (equalsIgnoreCase(value, (String)mspName.get("shortName"))) {
                retVal = true;
                break;
            }
        }

        return retVal;
    }

}

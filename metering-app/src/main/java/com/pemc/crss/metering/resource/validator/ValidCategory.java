package com.pemc.crss.metering.resource.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryValidator.class)
public @interface ValidCategory {

    String message() default "Invalid category. It should be either DAILY, MONTHLY, CORRECTED_DAILY, or CORRECTED_MONTHLY";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

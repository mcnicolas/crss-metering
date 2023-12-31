package com.pemc.crss.metering.resource.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = CategoryValidator.class)
public @interface ValidCategory {

    String message() default "Invalid category. It should be either DAILY, MONTHLY, CORRECTED_DAILY, or CORRECTED_MONTHLY";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

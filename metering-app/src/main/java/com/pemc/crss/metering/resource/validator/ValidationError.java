package com.pemc.crss.metering.resource.validator;

import lombok.Data;

@Data
public class ValidationError {

    private String code;
    private String message;

}

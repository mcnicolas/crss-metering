package com.pemc.crss.metering.resource.validator;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ErrorDetail {

    private String title;
    private int status;
    private String detail;
    private long timeStamp;
    private String path;
    private String developerMessage;
    private Map<String, List<ValidationError>> errors = new HashMap<>();

}

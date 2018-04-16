package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonPropertyOrder({"timestamp", "status", "missing_params", "message", "path"})
public class MissingParametersDto {

    @JsonProperty("timestamp")
    private final long timestamp = System.currentTimeMillis();

    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("path")
    private String path;

    @JsonIgnore
    private List<String> missingParamsList = new ArrayList<>();

    public MissingParametersDto(final HttpStatus httpStatus, final String path) {
        this.status = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.path = path;
    }


    public MissingParametersDto addToMissingParams(final String parameter, final String argument) {

        if (StringUtils.isBlank(argument)) {
            missingParamsList.add(parameter);
        }

        return this;
    }


    @JsonProperty("missing_params")
    private String getMissingParams() {
        return StringUtils.join(missingParamsList, ",");
    }
}

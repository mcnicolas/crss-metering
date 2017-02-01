package com.pemc.crss.commons.advice;


import lombok.Data;

@Data
public class ErrorResponseDto {

    public ErrorResponseDto(final String error) {
        this.error = error;
    }

    public ErrorResponseDto(final String error, final String cause) {
        this.error = error;
    }

    private String error;
}

package com.pemc.crss.commons.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto catchAllException(Exception e) {
        log.error(e.getMessage() == null ? "Uncaught exception." : e.getMessage(), e);
        String errorMessage = e.getMessage() == null ? "Internal Server Error" : e.getMessage();
        return new ErrorResponseDto(errorMessage);
    }
}

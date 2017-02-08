package com.pemc.crss.commons.advice;

import com.pemc.crss.metering.dao.exception.InvalidStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(InvalidStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto invalidState(InvalidStateException e) {
        log.error(e.getMessage() == null ? "Uncaught exception." : e.getMessage(), e);
        String errorMessage = e.getMessage() == null ? "Bad Request" : e.getMessage();
        return new ErrorResponseDto(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto catchAllException(Exception e) {
        log.error(e.getMessage() == null ? "Uncaught exception." : e.getMessage(), e);
        String errorMessage = e.getMessage() == null ? "Internal Server Error" : e.getMessage();
        return new ErrorResponseDto(errorMessage);
    }

}

package com.pemc.crss.metering.dao.exception;

public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String msg) {
        super(msg);
    }

    public InvalidStateException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

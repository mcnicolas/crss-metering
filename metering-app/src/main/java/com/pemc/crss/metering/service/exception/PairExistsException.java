package com.pemc.crss.metering.service.exception;

public class PairExistsException extends RuntimeException {

    public PairExistsException(String msg) {
        super(msg);
    }

    public PairExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

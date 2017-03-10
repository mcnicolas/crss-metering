package com.pemc.crss.metering.service.exception;

public class OldRecordException extends RuntimeException {

    public OldRecordException(String msg) {
        super(msg);
    }

    public OldRecordException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

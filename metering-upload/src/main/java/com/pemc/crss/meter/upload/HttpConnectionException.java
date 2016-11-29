package com.pemc.crss.meter.upload;

public class HttpConnectionException extends Exception {

    public HttpConnectionException(String message) {
        super(message);
    }

    public HttpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}

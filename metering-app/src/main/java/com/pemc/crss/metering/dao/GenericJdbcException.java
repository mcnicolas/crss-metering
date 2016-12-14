package com.pemc.crss.metering.dao;

import org.springframework.dao.DataAccessException;

public class GenericJdbcException extends DataAccessException {

    public GenericJdbcException(String msg) {
        super(msg);
    }

    public GenericJdbcException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

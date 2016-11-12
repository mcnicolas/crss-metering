package com.pemc.crss.metering.dao;

import lombok.Data;

@Data
public class MQBuilderData {

    private String sql;
    private Object[] arguments;

}

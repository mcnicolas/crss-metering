package com.pemc.crss.metering.dao.query;

public enum ComparisonOperator {

    EQUALS(" = "),
    NOT_EQUALS(" != "),
    GREATER_THAN(" > "),
    GREATER_THAN_EQUALS(" >= "),
    LESS_THAN(" < "),
    LESS_THAN_EQUALS(" <= "),
    LIKE(" LIKE "),
    IN(" IN "),
    NOT_IN(" NOT IN ");

    final String value;

    ComparisonOperator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

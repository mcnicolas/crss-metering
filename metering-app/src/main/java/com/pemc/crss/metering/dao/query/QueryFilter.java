package com.pemc.crss.metering.dao.query;

import lombok.Data;

import static com.pemc.crss.metering.dao.query.ComparisonOperator.EQUALS;


@Data
public class QueryFilter {

    private String column;
    private Object value;
    private ComparisonOperator operator;

    public QueryFilter(String column, Object value) {
        this(column, value, EQUALS);
    }

    public QueryFilter(String column, Object value, ComparisonOperator operator) {
        this.column = column;
        this.value = value;
        this.operator = operator;
    }

}

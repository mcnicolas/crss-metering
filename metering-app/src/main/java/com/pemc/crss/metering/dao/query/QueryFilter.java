package com.pemc.crss.metering.dao.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import static com.pemc.crss.metering.dao.query.ComparisonOperator.EQUALS;


@Data
@AllArgsConstructor
public class QueryFilter {

    private String column;
    private Object value;
    private ComparisonOperator operator;

    public QueryFilter(String column, Object value) {
        this(column, value, EQUALS);
    }

}

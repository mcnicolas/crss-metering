package com.pemc.crss.metering.dao.query;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.IN;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.NOT_IN;
import static java.util.Arrays.asList;
import static org.springframework.data.domain.Sort.Direction.ASC;

public class QueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private MapSqlParameterSource source = new MapSqlParameterSource();
    private int columnCount = 0;

    private static final String SELECT = "SELECT ";
    private static final String ALL = "*";
    private static final String COUNT = "COUNT(*)";
    private static final String AS = " AS ";
    private static final String COMMA = ", ";
    private static final String FROM = " FROM ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String WHERE = " WHERE ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String LIMIT = " LIMIT ";
    private static final String OFFSET = " OFFSET ";
    private static final String SPACE = " ";
    private static final String COLON = ":";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";

    public QueryBuilder select(String... columns) {
        sqlBuilder.append(SELECT);
        for (int i = 0; i < columns.length; i ++) {
            if (i > 0) {
                sqlBuilder.append(COMMA);
            }
            sqlBuilder.append(columns[i]);
        }
        return this;
    }

    public QueryBuilder select() {
        sqlBuilder.append(SELECT);
        return this;
    }

    public QueryBuilder all() {
        sqlBuilder.append(ALL);
        return this;
    }

    public QueryBuilder count() {
        sqlBuilder.append(COUNT);
        return this;
    }

    public QueryBuilder column(String column) {
        if (columnCount > 0) {
            sqlBuilder.append(COMMA);
        }
        sqlBuilder.append(column);
        columnCount ++;
        return this;
    }

    public QueryBuilder as(String alias) {
        sqlBuilder.append(AS);
        sqlBuilder.append(alias);
        return this;
    }

    public QueryBuilder from(String table) {
        sqlBuilder.append(FROM);
        sqlBuilder.append(table);
        return this;
    }

    public QueryBuilder where() {
        sqlBuilder.append(WHERE);
        return this;
    }

    public QueryBuilder where(String whereClause) {
        sqlBuilder.append(WHERE);
        sqlBuilder.append(whereClause);
        return this;
    }

    public QueryBuilder where(QueryFilter queryFilter) {
        return filter(WHERE, queryFilter);
    }

    public QueryBuilder and() {
        sqlBuilder.append(AND);
        return this;
    }

    public QueryBuilder and(String andStatement) {
        sqlBuilder.append(AND);
        sqlBuilder.append(andStatement);
        return this;
    }

    public QueryBuilder and(QueryFilter queryFilter) {
        return filter(AND, queryFilter);
    }

    public QueryBuilder or() {
        sqlBuilder.append(OR);
        return this;
    }

    public QueryBuilder or(String orStatement) {
        sqlBuilder.append(OR);
        sqlBuilder.append(orStatement);
        return this;
    }

    public QueryBuilder or(QueryFilter queryFilter) {
        return filter(OR, queryFilter);
    }

    public QueryBuilder openParenthesis() {
        sqlBuilder.append(OPEN_PARENTHESIS);
        return this;
    }

    public QueryBuilder openParenthesis(QueryFilter queryFilter) {
        sqlBuilder.append(OPEN_PARENTHESIS);
        return filter(queryFilter);
    }

    public QueryBuilder closeParenthesis() {
        sqlBuilder.append(CLOSE_PARENTHESIS);
        return this;
    }

    public QueryBuilder orderBy(List<PageOrder> pageOrderList) {
        sqlBuilder.append(ORDER_BY);
        for (int i = 0; i < pageOrderList.size(); i ++) {
            if (i > 0) {
                sqlBuilder.append(COMMA);
            }
            sqlBuilder.append(pageOrderList.get(i).getSortColumn());
            sqlBuilder.append(SPACE);
            sqlBuilder.append(pageOrderList.get(i).getSortDirection().toString());
        }
        return this;
    }

    public QueryBuilder orderBy(String column) {
        return orderBy(column, ASC);
    }

    public QueryBuilder orderBy(String column, Direction direction) {
        sqlBuilder.append(ORDER_BY);
        sqlBuilder.append(column);
        sqlBuilder.append(SPACE);
        sqlBuilder.append(direction.toString());
        return this;
    }

    public QueryBuilder paginate(int pageNo, int pageSize) {
        sqlBuilder.append(LIMIT);
        sqlBuilder.append((pageNo + 1) * pageSize);
        sqlBuilder.append(OFFSET);
        sqlBuilder.append(pageNo * pageSize);
        return this;
    }

    private QueryBuilder filter(String clause, QueryFilter queryFilter) {
        sqlBuilder.append(clause);
        return filter(queryFilter);
    }

    public QueryBuilder filter(String customFilter) {
        sqlBuilder.append(customFilter);
        return this;
    }

    public QueryBuilder filter(QueryFilter queryFilter) {
        String paramName = getParamName(queryFilter.getColumn());
        sqlBuilder.append(queryFilter.getColumn());
        ComparisonOperator operator = queryFilter.getOperator();
        if (asList(IN, NOT_IN).indexOf(operator) > -1) {
            sqlBuilder.append(operator.getValue());
            sqlBuilder.append(OPEN_PARENTHESIS);
            sqlBuilder.append(COLON);
            sqlBuilder.append(paramName);
            sqlBuilder.append(CLOSE_PARENTHESIS);
        } else {
            sqlBuilder.append(operator.getValue());
            sqlBuilder.append(COLON);
            sqlBuilder.append(paramName);
        }
        source.addValue(paramName, queryFilter.getValue());
        return this;
    }

    public QueryData build() {
        return new QueryData(sqlBuilder.toString(), source);
    }

    private String getParamName(String columnName) {
        return toCamelCase(removeLeadingText(removeParenthesis(columnName)));
    }

    private String toCamelCase(String value) {
        return UPPER_UNDERSCORE.to(LOWER_CAMEL, value);
    }

    private String removeParenthesis(String value) {
        return value.replaceAll("[()]", "");
    }

    private String removeLeadingText(String value) {
        if (value.indexOf('.') > -1) {
            return value.substring(value.indexOf('.') + 1);
        }
        return value;
    }

}

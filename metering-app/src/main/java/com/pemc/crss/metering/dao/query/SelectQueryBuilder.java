package com.pemc.crss.metering.dao.query;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.IN;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.NOT_IN;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.NONE;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Data
public class SelectQueryBuilder implements QueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private MapSqlParameterSource source = new MapSqlParameterSource();

    @Getter(NONE)
    @Setter(NONE)
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

    public SelectQueryBuilder() {
        this.sqlBuilder = new StringBuilder(SELECT);}

    public SelectQueryBuilder(final String initQuery) {
        this.sqlBuilder = new StringBuilder(initQuery);
    }

    public SelectQueryBuilder withSource(MapSqlParameterSource source) {
        this.source = source;
        return this;
    }

    @Override
    public QueryData build() {
        return new QueryData(sqlBuilder.toString(), source);
    }

    public SelectQueryBuilder select() {
        sqlBuilder.append(SELECT);
        return this;
    }

    public SelectQueryBuilder all() {
        sqlBuilder.append(ALL);
        return this;
    }

    public SelectQueryBuilder count() {
        sqlBuilder.append(COUNT);
        return this;
    }

    public SelectQueryBuilder column(String column) {
        if (columnCount > 0) {
            sqlBuilder.append(COMMA);
        }
        sqlBuilder.append(column);
        columnCount ++;
        return this;
    }

    public SelectQueryBuilder as(String alias) {
        sqlBuilder.append(AS);
        sqlBuilder.append(alias);
        return this;
    }

    public SelectQueryBuilder from(String table) {
        sqlBuilder.append(FROM);
        sqlBuilder.append(table);
        return this;
    }

    public SelectQueryBuilder where() {
        sqlBuilder.append(WHERE);
        return this;
    }

    public SelectQueryBuilder and() {
        sqlBuilder.append(AND);
        return this;
    }

    public SelectQueryBuilder or() {
        sqlBuilder.append(OR);
        return this;
    }

    public SelectQueryBuilder openParenthesis() {
        sqlBuilder.append(OPEN_PARENTHESIS);
        return this;
    }

    public SelectQueryBuilder closeParenthesis() {
        sqlBuilder.append(CLOSE_PARENTHESIS);
        return this;
    }

    public SelectQueryBuilder orderBy(List<PageOrder> pageOrderList) {
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

    public SelectQueryBuilder orderBy(String column) {
        return orderBy(column, ASC);
    }

    public SelectQueryBuilder orderBy(String column, Direction direction) {
        sqlBuilder.append(ORDER_BY);
        sqlBuilder.append(column);
        sqlBuilder.append(SPACE);
        sqlBuilder.append(direction.toString());
        return this;
    }

    public SelectQueryBuilder paginate(int pageNo, int pageSize) {
        sqlBuilder.append(LIMIT);
        sqlBuilder.append((pageNo + 1) * pageSize);
        sqlBuilder.append(OFFSET);
        sqlBuilder.append(pageNo * pageSize);
        return this;
    }

    public SelectQueryBuilder filter(String customFilter) {
        sqlBuilder.append(customFilter);
        return this;
    }

    public SelectQueryBuilder filter(QueryFilter queryFilter) {
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

    private String getParamName(String columnName) {
        return toCamelCase(removeLeadingText(removeParenthesis(removeFunction(columnName))));
    }

    private String removeFunction(String value) {
        if (value.indexOf('(') > -1) {
            return value.substring(value.indexOf('('));
        }
        return value;
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

    private String toCamelCase(String value) {
        return UPPER_UNDERSCORE.to(LOWER_CAMEL, value);
    }

}

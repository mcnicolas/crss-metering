package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;
import com.pemc.crss.metering.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.utils.DateTimeUtils.dateToLong;
import static com.pemc.crss.metering.utils.DateTimeUtils.endOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.endOfMonth;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfMonth;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MQDisplayQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List arguments = new ArrayList();

    public MQDisplayQueryBuilder selectMeterData(String category, String readingDate) {
        String selectSQL = "SELECT B.*" +
                " FROM TXN_MQ_MANIFEST_FILE AS A" +
                " INNER JOIN ${MQ_TABLE} AS B ON A.FILE_ID = B.FILE_ID";

        String tableName = getTableName(category);
        selectSQL = selectSQL.replace("${MQ_TABLE}", tableName);
        sqlBuilder.append(selectSQL);
        sqlBuilder.append(" WHERE READING_DATETIME BETWEEN ? AND ?");
        addReadingDateFilter(category, readingDate);

        return this;
    }

    public MQDisplayQueryBuilder countMeterData(String category, String readingDate) {
        String countSQL = "SELECT COUNT(B.SEIN)" +
                " FROM TXN_MQ_MANIFEST_FILE AS A" +
                " INNER JOIN ${MQ_TABLE} AS B ON A.FILE_ID = B.FILE_ID";

        String tableName = getTableName(category);
        countSQL = countSQL.replace("${MQ_TABLE}", tableName);
        sqlBuilder.append(countSQL);
        sqlBuilder.append(" WHERE READING_DATETIME BETWEEN ? AND ?");
        addReadingDateFilter(category, readingDate);

        return this;
    }

    public MQDisplayQueryBuilder addSEINFilter(String sein) {
        if (isNotBlank(sein)) {
            sqlBuilder.append(" AND SEIN LIKE ?");
            arguments.add(sein + "%");
        }

        return this;
    }

    public MQDisplayQueryBuilder addTransactionIDFilter(String transactionID) {
        if (isNotBlank(transactionID)) {
            sqlBuilder.append(" AND A.TRANSACTION_ID LIKE ?");
            arguments.add(transactionID + "%");
        }

        return this;
    }

    public MQDisplayQueryBuilder orderBy(List<PageOrder> pageOrderList) {
        if (isNotEmpty(pageOrderList)) {
            sqlBuilder.append(" ORDER BY ?");

            PageOrder pageOrder = pageOrderList.get(0);
            arguments.add(pageOrder.getSortColumn());

            switch (pageOrder.getSortDirection()) {
                case ASC:
                    sqlBuilder.append(" ASC");
                    break;
                case DESC:
                    sqlBuilder.append(" DESC");
            }
        }

        return this;
    }

    public MQBuilderData build() {
        MQBuilderData retVal = new MQBuilderData();
        retVal.setSql(sqlBuilder.toString());
        retVal.setArguments(arguments.toArray());

        return retVal;
    }

    private void addReadingDateFilter(String category, String readingDate) {
        Date dateParam = DateTimeUtils.parseDate(readingDate);

        Date startDate = new Date();
        Date endDate = new Date();

        if (equalsIgnoreCase(category, "monthly")) {
            startDate = startOfMonth(dateParam);
            endDate = endOfMonth(dateParam);
        } else if (equalsIgnoreCase(category, "daily")) {
            startDate = startOfDay(dateParam);
            endDate = endOfDay(dateParam);
        }

        arguments.add(dateToLong(startDate));
        arguments.add(dateToLong(endDate));
    }

    private String getTableName(String category) {
        String tableName = "";
        if (equalsIgnoreCase(category, "monthly")) {
            tableName = "TXN_METER_DATA_MONTHLY";
        } else if (equalsIgnoreCase(category, "daily")) {
            tableName = "TXN_METER_DATA_DAILY";
        }

        return tableName;
    }

}

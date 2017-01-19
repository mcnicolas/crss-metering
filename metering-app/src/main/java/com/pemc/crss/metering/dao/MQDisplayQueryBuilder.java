package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;
import com.pemc.crss.metering.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import static com.pemc.crss.metering.utils.DateTimeUtils.dateToLong;
import static com.pemc.crss.metering.utils.DateTimeUtils.endOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.endOfMonth;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDayMQ;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfMonth;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MQDisplayQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List arguments = new ArrayList();

    public MQDisplayQueryBuilder selectMeterData(String category, String readingDate) {
        String selectSQL = "SELECT DISTINCT ON (B.SEIN, B.READING_DATETIME)"
                + " B.METER_DATA_ID, B.SEIN, A.TRANSACTION_ID, B.READING_DATETIME,"
                + " B.KWD, B.KWHD, B.KVARHD, B.KWR, B.KWHR, B.KVARHR, B.ESTIMATION_FLAG,"
                + " MAX(B.CREATED_DATE_TIME) OVER (PARTITION BY B.SEIN, B.READING_DATETIME ORDER BY B.CREATED_DATE_TIME DESC)"
                + " FROM TXN_MQ_MANIFEST_FILE A"
                + " INNER JOIN ${MQ_TABLE} B ON A.FILE_ID = B.FILE_ID";

        String tableName = getTableName(category);
        selectSQL = selectSQL.replace("${MQ_TABLE}", tableName);
        sqlBuilder.append(selectSQL);
        sqlBuilder.append(" WHERE B.READING_DATETIME BETWEEN ? AND ?");

        addReadingDateFilter(category, readingDate);

        return this;
    }

    public MQDisplayQueryBuilder countMeterData(String category, String readingDate) {
        String countSQL = "SELECT COUNT(DISTINCT(B.SEIN, B.READING_DATETIME))"
                + " FROM TXN_MQ_MANIFEST_FILE A"
                + " INNER JOIN ${MQ_TABLE} B ON A.FILE_ID = B.FILE_ID";

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
            arguments.add("%" + sein + "%");
        }

        return this;
    }

    public MQDisplayQueryBuilder addTransactionIDFilter(String transactionID) {
        if (isNotBlank(transactionID)) {
            sqlBuilder.append(" AND A.TRANSACTION_ID LIKE ?");
            arguments.add("%" + transactionID + "%");
        }

        return this;
    }

    public MQDisplayQueryBuilder addMSPFilter(String mspShortName) {
        if (isNotBlank(mspShortName)) {
            sqlBuilder.append(" AND B.MSP_SHORTNAME LIKE ?");
            arguments.add("%" + mspShortName + "%");
        }

        return this;
    }

    public MQDisplayQueryBuilder orderBy(List<PageOrder> pageOrderList) {
        if (isNotEmpty(pageOrderList)) {
            sqlBuilder.append(" ORDER BY ");

            StringJoiner joiner = new StringJoiner(",");
            for (PageOrder pageOrder : pageOrderList) {
                joiner.add(pageOrder.getSortColumn() + " " + pageOrder.getSortDirection());
            }

            sqlBuilder.append(joiner.toString());
        }

        return this;
    }

    public BuilderData build() {
        BuilderData retVal = new BuilderData();
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
            startDate = startOfDayMQ(dateParam);
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

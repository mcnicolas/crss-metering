package com.pemc.crss.metering.dao;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MQVersionQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List arguments = new ArrayList();

    public MQVersionQueryBuilder selectVersionData(String category, Long readingDateFrom, Long readingDateTo) {
        String selectSQL = "SELECT TRANSACTION_ID, TO_CHAR(CREATED_DATE_TIME, 'YYYY-MM-DD HH24:MI') AS CREATED_DATE_TIME"
                + " FROM"
                + "  (SELECT DISTINCT ON (A.TRANSACTION_ID, C.CREATED_DATE_TIME)"
                + "      A.TRANSACTION_ID, C.SEIN, C.MSP_SHORTNAME, C.CREATED_DATE_TIME,"
                + "      COUNT(C.SEIN) OVER (PARTITION BY C.SEIN, C.READING_DATETIME) AS RECORD_COUNT"
                + "    FROM TXN_MQ_MANIFEST_HEADER A"
                + "      INNER JOIN TXN_MQ_MANIFEST_FILE B ON A.HEADER_ID = B.HEADER_ID"
                + "      INNER JOIN ${MQ_TABLE} C ON B.FILE_ID = C.FILE_ID";

        String tableName = getTableName(category);
        selectSQL = selectSQL.replace("${MQ_TABLE}", tableName);
        sqlBuilder.append(selectSQL);

        if (readingDateFrom != null && readingDateTo != null) {
            sqlBuilder.append(" WHERE C.READING_DATETIME BETWEEN ? AND ?");

            arguments.add(readingDateFrom);
            arguments.add(readingDateTo);
        }

        sqlBuilder.append(" ) AS VERSIONED")
                .append(" WHERE RECORD_COUNT > 1");

        return this;
    }

    public MQVersionQueryBuilder addSEINFilter(String sein) {
        if (isNotBlank(sein)) {
            sqlBuilder.append(" AND SEIN LIKE ?");
            arguments.add("%" + sein + "%");
        }

        return this;
    }

    public MQVersionQueryBuilder addTransactionIDFilter(String transactionID) {
        if (isNotBlank(transactionID)) {
            sqlBuilder.append(" AND TRANSACTION_ID LIKE ?");
            arguments.add("%" + transactionID + "%");
        }

        return this;
    }

    public MQVersionQueryBuilder addMSPFilter(String mspShortName) {
        if (isNotBlank(mspShortName)) {
            sqlBuilder.append(" AND MSP_SHORTNAME LIKE ?");
            arguments.add("%" + mspShortName + "%");
        }

        return this;
    }

    public MQVersionQueryBuilder orderBy() {
        sqlBuilder.append(" ORDER BY CREATED_DATE_TIME DESC");

        return this;
    }

    public BuilderData build() {
        BuilderData retVal = new BuilderData();
        retVal.setSql(sqlBuilder.toString());
        retVal.setArguments(arguments.toArray());

        return retVal;
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

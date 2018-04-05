package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;

public class MQExportQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List arguments = new ArrayList();

    public MQExportQueryBuilder selectMeterData(String category, Long readingDateFrom, Long readingDateTo, boolean isLatest) {
        sqlBuilder.append("SELECT DISTINCT ON (B.SEIN, B.READING_DATETIME)")
                .append(" B.CATEGORY, B.MSP_SHORTNAME, B.SEIN, B.TRADE_PARTICIPANT_SHORT_NAME, B.READING_DATETIME, B.KWHD,")
                .append(" B.KVARHD, B.KWD, B.KWHR, B.KVARHR, B.KWR, B.ESTIMATION_FLAG, B.UPLOAD_DATETIME, B.TRANSACTION_ID");

        if (isLatest) {
            sqlBuilder.append(", MAX(B.CREATED_DATE_TIME) OVER (PARTITION BY B.SEIN, B.READING_DATETIME ORDER BY B.CREATED_DATE_TIME DESC)");
        }

        sqlBuilder.append(" FROM ")
                .append(getTableName(category))
                .append(" B ");

        if (readingDateFrom != null && readingDateTo != null) {
            sqlBuilder.append(" WHERE B.READING_DATETIME BETWEEN ? AND ?");

            arguments.add(readingDateFrom);
            arguments.add(readingDateTo);
        }

        return this;
    }


    public MQExportQueryBuilder countMeterData(String category, Long readingDateFrom, Long readingDateTo) {
        String countSQL = "SELECT COUNT(DISTINCT(B.SEIN, B.READING_DATETIME))"
                + " FROM ${MQ_TABLE} ";

        String tableName = getTableName(category);
        countSQL = countSQL.replace("${MQ_TABLE}", tableName);
        sqlBuilder.append(countSQL);

        if (readingDateFrom != null && readingDateTo != null) {
            sqlBuilder.append(" WHERE READING_DATETIME BETWEEN ? AND ?");

            arguments.add(readingDateFrom);
            arguments.add(readingDateTo);
        }

        return this;
    }

    public MQExportQueryBuilder addSEINFilter(String sein) {
        if (isNotBlank(sein)) {
            sqlBuilder.append(" AND UPPER(SEIN) LIKE ?");
            arguments.add("%" + sein.toUpperCase() + "%");
        }

        return this;
    }

    public MQExportQueryBuilder addTpShortnameFilter(String tpShortname) {
        if (isNotBlank(tpShortname)) {
            sqlBuilder.append(" AND  trade_participant_short_name = ?");
            arguments.add(tpShortname);
        }

        return this;
    }


    public MQExportQueryBuilder orderBy(List<PageOrder> pageOrderList) {
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

    public MQExportQueryBuilder paginate(int pageNo, int pageSize) {
        sqlBuilder.append(" LIMIT ");
        sqlBuilder.append((pageNo + 1) * pageSize);
        sqlBuilder.append(" OFFSET ");
        sqlBuilder.append(pageNo * pageSize);
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
            tableName = "vw_mq_extraction_monthly";
        } else if (equalsIgnoreCase(category, "daily")) {
            tableName = "vw_mq_extraction_daily";
        }

        return tableName;
    }

}

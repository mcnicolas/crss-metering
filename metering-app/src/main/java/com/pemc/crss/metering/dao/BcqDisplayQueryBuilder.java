package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BcqDisplayQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List<Object> arguments = new ArrayList<>();
    private String selectQuery;
    private String countQuery;
    private String paginationQuery;

    public BcqDisplayQueryBuilder(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public BcqDisplayQueryBuilder(String selectQuery, String countQuery, String paginationQuery) {
        this.selectQuery = selectQuery;
        this.countQuery = countQuery;
        this.paginationQuery = paginationQuery;
    }

    public BcqDisplayQueryBuilder selectBcqDeclarationsByHeaderId(long headerId) {
        sqlBuilder.append(selectQuery);
        sqlBuilder.append(" WHERE A.BCQ_HEADER_ID = ?");
        arguments.add(headerId);

        return this;
    }

    public BcqDisplayQueryBuilder selectBcqDeclarationsByTradingDate(Date tradingDate) {
        sqlBuilder.append(selectQuery);
        sqlBuilder.append(" WHERE A.TRADING_DATE = ?");
        arguments.add(tradingDate);

        return this;
    }

    public BcqDisplayQueryBuilder countBcqDeclarationsByHeaderId(long headerId) {
        sqlBuilder.append(countQuery);
        sqlBuilder.append(" WHERE A.BCQ_HEADER_ID = ?");
        arguments.add(headerId);

        return this;
    }

    public BcqDisplayQueryBuilder countBcqDeclarationsByTradingDate(Date tradingDate) {
        sqlBuilder.append(countQuery);
        sqlBuilder.append(" WHERE A.TRADING_DATE = ?");
        arguments.add(tradingDate);

        return this;
    }

    public BcqDisplayQueryBuilder addSellingMtnFilter(String sellingMtn) {
        if (isNotBlank(sellingMtn)) {
            sqlBuilder.append(" AND UPPER(A.SELLING_MTN) LIKE ?");
            arguments.add("%" + sellingMtn.toUpperCase() + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addSellingParticipantFilter(String sellingParticipant) {
        if (isNotBlank(sellingParticipant)) {
            sqlBuilder.append(" AND UPPER(A.SELLING_PARTICIPANT_SHORT_NAME) LIKE ?");
            arguments.add("%" + sellingParticipant.toUpperCase() + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addBuyingParticipantFilter(String buyingParticipant) {
        if (isNotBlank(buyingParticipant)) {
            sqlBuilder.append(" AND UPPER(A.BUYING_PARTICIPANT_SHORT_NAME) LIKE ?");
            arguments.add("%" + buyingParticipant.toUpperCase() + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addStatusFilter(String status) {
        if (isNotBlank(status)) {
            sqlBuilder.append(" AND A.STATUS = ? ");
            arguments.add(status);
        }

        return this;
    }

    public BcqDisplayQueryBuilder orderBy(List<PageOrder> pageOrderList) {
        if (isNotEmpty(pageOrderList)) {
            for (PageOrder pageOrder : pageOrderList) {
                sqlBuilder.append(" ORDER BY ");
                sqlBuilder.append(pageOrder.getSortColumn());
                sqlBuilder.append(" ").append(pageOrder.getSortDirection().toString());
            }
        }

        return this;
    }

    public BcqDisplayQueryBuilder paginate(int pageNo, int pageSize) {
        String selectQuery = sqlBuilder.toString();
        paginationQuery = paginationQuery.replace("{SELECT_QUERY}", selectQuery);
        paginationQuery = paginationQuery.replace("{PAGE_NO}", String.valueOf(pageNo));
        paginationQuery = paginationQuery.replace("{PAGE_SIZE}", String.valueOf(pageSize));
        sqlBuilder = new StringBuilder(paginationQuery);

        return this;
    }

    public BuilderData build() {
        BuilderData data = new BuilderData();
        data.setSql(sqlBuilder.toString());
        data.setArguments(arguments.toArray());

        return data;
    }

}

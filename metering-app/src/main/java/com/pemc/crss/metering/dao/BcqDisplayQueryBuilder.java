package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BcqDisplayQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List<Object> arguments = new ArrayList<>();
    private String paginationWrapper;
    private boolean hasQuery = false;
    private boolean hasWhereClause = false;

    private static final String AND = " AND ";
    private static final String WHERE = " WHERE ";

    public BcqDisplayQueryBuilder() {
    }

    public BcqDisplayQueryBuilder(String paginationWrapper) {
        this.paginationWrapper = paginationWrapper;
    }

    public BcqDisplayQueryBuilder newQuery(String query) {
        if (hasQuery) {
            throw new IllegalArgumentException("Only one query must be present.");
        }

        if (!isNotBlank(query)) {
            throw new IllegalArgumentException("Query cannot be blank");
        }

        sqlBuilder.append(query);
        hasQuery = true;
        return this;
    }

    public BcqDisplayQueryBuilder addHeaderIdFilter(Long headerId) {
        if (headerId != null) {
            sqlBuilder.append(formatFilter("A.BCQ_HEADER_ID = ?"));
            arguments.add(headerId);
        }
        return this;
    }

    public BcqDisplayQueryBuilder addTradingDateFilter(Date tradingDate) {
        if (tradingDate != null) {
            sqlBuilder.append(formatFilter("A.TRADING_DATE = ?"));
            arguments.add(tradingDate);
        }
        return this;
    }

    public BcqDisplayQueryBuilder addSellingMtnFilter(String sellingMtn) {
        if (isNotBlank(sellingMtn)) {
            sqlBuilder.append(formatFilter("UPPER(A.SELLING_MTN) LIKE ?"));
            arguments.add("%" + sellingMtn.toUpperCase() + "%");
        }
        return this;
    }

    public BcqDisplayQueryBuilder addBillingIdFilter(String billingId) {
        if (isNotBlank(billingId)) {
            sqlBuilder.append(formatFilter("UPPER(A.BILLING_ID) LIKE ?"));
            arguments.add("%" + billingId.toUpperCase() + "%");
        }
        return this;
    }

    public BcqDisplayQueryBuilder addSellingParticipantFilter(String sellingParticipant) {
        if (isNotBlank(sellingParticipant)) {
            sqlBuilder.append(formatFilter("UPPER(A.SELLING_PARTICIPANT_SHORT_NAME) LIKE ?"));
            arguments.add("%" + sellingParticipant.toUpperCase() + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addBuyingParticipantFilter(String buyingParticipant) {
        if (isNotBlank(buyingParticipant)) {
            sqlBuilder.append(formatFilter("UPPER(A.BUYING_PARTICIPANT_SHORT_NAME) LIKE ?"));
            arguments.add("%" + buyingParticipant.toUpperCase() + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addStatusFilter(String status) {
        if (isNotBlank(status)) {
            sqlBuilder.append(formatFilter("A.STATUS = ? "));
            arguments.add(status);
        }
        return this;
    }

    public BcqDisplayQueryBuilder orderBy(List<PageOrder> pageOrderList) {
        checkQuery();
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
        checkQuery();
        if (isBlank(paginationWrapper)) {
            throw new IllegalArgumentException("Pagination wrapper is blank.");
        }
        String selectQuery = sqlBuilder.toString();
        paginationWrapper = paginationWrapper.replace("{SELECT_QUERY}", selectQuery);
        paginationWrapper = paginationWrapper.replace("{PAGE_NO}", String.valueOf(pageNo));
        paginationWrapper = paginationWrapper.replace("{PAGE_SIZE}", String.valueOf(pageSize));
        sqlBuilder = new StringBuilder(paginationWrapper);
        return this;
    }

    public BuilderData build() {
        checkQuery();
        BuilderData data = new BuilderData();
        data.setSql(sqlBuilder.toString());
        data.setArguments(arguments.toArray());
        return data;
    }

    private void checkQuery() {
        if (!hasQuery) {
            throw new IllegalArgumentException("Query is missing.");
        }
    }

    private String formatFilter(String filter) {
        checkQuery();
        if (hasWhereClause) {
            return AND + filter;
        } else {
            hasWhereClause = true;
            return WHERE + filter;
        }
    }

}

package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BcqDisplayQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List<Object> arguments = new ArrayList<>();
    private String displayData;
    private String displayCount;
    private String displayWrapper;

    public BcqDisplayQueryBuilder(String displayData, String displayCount, String displayWrapper) {
        this.displayData = displayData;
        this.displayCount = displayCount;
        this.displayWrapper = displayWrapper;
    }

    public BcqDisplayQueryBuilder selectBcqDeclarations(Date tradingDate) {
        sqlBuilder.append(displayData);
        arguments.add(tradingDate);

        return this;
    }

    public BcqDisplayQueryBuilder countBcqDeclarations(Date tradingDate) {
        sqlBuilder.append(displayCount);
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
            sqlBuilder.append(" AND UPPER(A.BUYING_PARTICIPANT) LIKE ?");
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
                sqlBuilder.append(" ORDER BY ?");
                sqlBuilder.append(" ").append(pageOrder.getSortDirection().toString());
                arguments.add(pageOrder.getSortColumn());
            }
        }

        return this;
    }

    public BcqDisplayQueryBuilder paginate(int pageNo, int pageSize) {
        String selectQuery = sqlBuilder.toString();
        displayWrapper = displayWrapper.replace("{SELECT_QUERY}", selectQuery);
        displayWrapper = displayWrapper.replace("{PAGE_NO}", String.valueOf(pageNo));
        displayWrapper = displayWrapper.replace("{PAGE_SIZE}", String.valueOf(pageSize));
        sqlBuilder = new StringBuilder(displayWrapper);

        return this;
    }

    public BuilderData build() {
        BuilderData data = new BuilderData();
        data.setSql(sqlBuilder.toString());
        data.setArguments(arguments.toArray());

        return data;
    }

}

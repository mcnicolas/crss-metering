package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BcqDisplayQueryBuilder {

    private StringBuilder sqlBuilder = new StringBuilder();
    private List<Object> arguments = new ArrayList<>();

    @Value("${bcq.display.data}")
    private String displayData;

    @Value("${bcq.display.count}")
    private String displayCount;

    @Value("${bcq.display.pagination}")
    private String displayPagination;

    public BcqDisplayQueryBuilder selectBcqDeclarations(Date tradingDate) {
        sqlBuilder.append(displayData);
        arguments.add(tradingDate);

        return this;
    }

    public BcqDisplayQueryBuilder countBcqDelarations(Date tradingDate) {
        sqlBuilder.append(displayCount);
        arguments.add(tradingDate);

        return this;
    }

    public BcqDisplayQueryBuilder addSellingMtnFilter(String sellingMtn) {
        if (isNotBlank(sellingMtn)) {
            sqlBuilder.append(" AND A.SELLING_MTN LIKE ?");
            arguments.add("%" + sellingMtn + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addSellingParticipantFilter(String sellingParticipant) {
        if (isNotBlank(sellingParticipant)) {
            sqlBuilder.append(" AND A.SELLING_PARTICIPANT_SHORT_NAME LIKE ?");
            arguments.add("%" + sellingParticipant + "%");
        }

        return this;
    }

    public BcqDisplayQueryBuilder addBuyingParticipantFilter(String buyingParticipant) {
        if (isNotBlank(buyingParticipant)) {
            sqlBuilder.append(" AND A.BUYING_PARTICIPANT LIKE ?");
            arguments.add("%" + buyingParticipant + "%");
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
        sqlBuilder.append(" ").append(displayPagination);
        arguments.add(pageNo * pageSize);
        arguments.add(pageSize);

        return this;
    }

    public BuilderData build() {
        BuilderData data = new BuilderData();
        data.setSql(sqlBuilder.toString());
        data.setArguments(arguments.toArray());

        return data;
    }

}

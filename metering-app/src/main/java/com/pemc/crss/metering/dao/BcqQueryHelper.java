package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dao.query.QueryBuilder;
import com.pemc.crss.metering.dao.query.QueryData;
import com.pemc.crss.metering.dao.query.QueryFilter;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.lang.Long.parseLong;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class BcqQueryHelper {

    private static final String UNIQUE_HEADER_ID_QUERY =
            "SELECT DISTINCT ON (SELLING_MTN, BILLING_ID, TRADING_DATE) HEADER_ID FROM TXN_BCQ_HEADER A"
                    + " INNER JOIN TXN_BCQ_UPLOAD_FILE B ON A.FILE_ID = B.FILE_ID"
                    + " ORDER BY SELLING_MTN, BILLING_ID, TRADING_DATE, SUBMITTED_DATE DESC";

    private static final String HEADER_JOIN_FILE =
            "TXN_BCQ_HEADER A INNER JOIN TXN_BCQ_UPLOAD_FILE B ON A.FILE_ID = B.FILE_ID";

    public QueryData queryHeaderPageDisplay(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        String status = getValue(mapParams, "status");
        boolean isSettlement = getValue(mapParams, "isSettlement") != null;
        QueryData uniqueHeaderIdQueryData = queryUniqueHeaderIds(mapParams);
        QueryBuilder queryBuilder = new QueryBuilder()
                .select()
                    .column("HEADER_ID")
                    .column("SELLING_MTN")
                    .column("BILLING_ID")
                    .column("TO_CHAR(TRADING_DATE, 'YYYY-MM-DD')").as("TRADING_DATE")
                    .column("SELLING_PARTICIPANT_USER_ID")
                    .column("SELLING_PARTICIPANT_NAME")
                    .column("SELLING_PARTICIPANT_SHORT_NAME")
                    .column("BUYING_PARTICIPANT_USER_ID")
                    .column("BUYING_PARTICIPANT_NAME")
                    .column("BUYING_PARTICIPANT_SHORT_NAME")
                    .column(subQuery("STRING_AGG(D.TRANSACTION_ID, ', ' ORDER BY D.SUBMITTED_DATE)", status,
                            isSettlement)).as("TRANSACTION_ID")
                    .column(subQuery("STRING_AGG(TO_CHAR(D.SUBMITTED_DATE, 'YYYY-MM-DD hh:MI AM'), ', ' "
                            + "ORDER BY D.SUBMITTED_DATE)", status, isSettlement)).as("SUBMITTED_DATE")
                    .column(subQuery("STRING_AGG(COALESCE(TO_CHAR(C.DEADLINE_DATE, 'YYYY-MM-DD'), ' '), ', ' "
                            + "ORDER BY D.SUBMITTED_DATE)", status, isSettlement)).as("DEADLINE_DATE")
                    .column(subQuery("STRING_AGG(C.STATUS, ', ' ORDER BY D.SUBMITTED_DATE)", status, isSettlement))
                        .as("STATUS")
                    .column(subQuery("STRING_AGG(COALESCE(C.UPDATED_VIA, ' '), ', ' ORDER BY D.SUBMITTED_DATE)", status,
                            isSettlement)).as("UPDATED_VIA")
                .from(HEADER_JOIN_FILE)
                .where().filter("HEADER_ID IN(" + uniqueHeaderIdQueryData.getSql() + ")")
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageableRequest.getPageNo(), pageableRequest.getPageSize());
        queryBuilder.setSource(uniqueHeaderIdQueryData.getSource());
        return queryBuilder.build();
    }

    public QueryData queryHeaderPageCount(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        QueryData uniqueHeaderIdQueryData = queryUniqueHeaderIds(mapParams);
        QueryBuilder queryBuilder = new QueryBuilder()
                .select().count()
                .from(HEADER_JOIN_FILE)
                .where().filter("HEADER_ID IN(" + uniqueHeaderIdQueryData.getSql() + ")");
        queryBuilder.setSource(uniqueHeaderIdQueryData.getSource());
        return queryBuilder.build();
    }

    private QueryData queryUniqueHeaderIds(Map<String, String> mapParams) {
        QueryBuilder queryBuilder = new QueryBuilder()
                .select().column("DISTINCT ON (SELLING_MTN, BILLING_ID, TRADING_DATE) HEADER_ID")
                .from(HEADER_JOIN_FILE);
        return addFilters(queryBuilder, mapParams).build();
    }

    private String subQuery(String query, String status, boolean isSettlement) {
        QueryBuilder queryBuilder = new QueryBuilder()
                .select().column(query)
                .from("TXN_BCQ_HEADER C INNER JOIN TXN_BCQ_UPLOAD_FILE D ON C.FILE_ID = D.FILE_ID")
                .where().filter("A.SELLING_MTN = C.SELLING_MTN")
                    .and().filter("A.SELLING_MTN = C.SELLING_MTN")
                    .and().filter("A.BILLING_ID = C.BILLING_ID")
                    .and().filter("A.TRADING_DATE = C.TRADING_DATE")
                    .and();
        if (status == null) {
            List<String> statusList;
            if (isSettlement) {
                statusList = of(VOID).map(Enum::toString).collect(toList());
            } else {
                statusList = of(VOID, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATED, FOR_APPROVAL_CANCEL).map(Enum::toString)
                        .collect(toList());
            }
            queryBuilder = queryBuilder.filter(new QueryFilter("C.STATUS", statusList, NOT_IN));
        } else {
            queryBuilder = queryBuilder.filter(new QueryFilter("C.STATUS", status));
        }
        return "(" + queryBuilder.build().getSql() + ")";
    }

    public QueryBuilder addFilters(QueryBuilder queryBuilder, Map<String, String> mapParams) {
        String status = getValue(mapParams, "status");
        if (status == null) {
            queryBuilder = addIsSettlementFilter(queryBuilder, getValue(mapParams, "isSettlement") != null);
        } else {
            queryBuilder = addStatusFilter(queryBuilder, getValue(mapParams, "status"));
        }
        queryBuilder = addHeaderIdFilter(queryBuilder, getValue(mapParams, "headerId", Long.class));
        queryBuilder = addTradingDateFilter(queryBuilder, getValue(mapParams, "tradingDate", Date.class));
        queryBuilder = addSellingMtnFilter(queryBuilder, getValue(mapParams, "sellingMtn"));
        queryBuilder = addBillingIdFilter(queryBuilder, getValue(mapParams, "billingId"));
        queryBuilder = addSellingParticipantFilter(queryBuilder, getValue(mapParams, "sellingParticipant"));
        queryBuilder = addBuyingParticipantFilter(queryBuilder, getValue(mapParams, "buyingParticipant"));
        queryBuilder = addExpiredFilter(queryBuilder, getValue(mapParams, "expired") != null);
        return queryBuilder;
    }

    private QueryBuilder addIsSettlementFilter(QueryBuilder queryBuilder, boolean isSettlement) {
        List<String> statusList;
        if (isSettlement) {
            statusList = of(VOID).map(Enum::toString).collect(toList());
        } else {
            statusList = of(VOID, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATED, FOR_APPROVAL_CANCEL).map(Enum::toString)
                    .collect(toList());
        }
        return queryBuilder.where().filter(new QueryFilter("STATUS", statusList, NOT_IN));
    }

    private QueryBuilder addStatusFilter(QueryBuilder queryBuilder, String status) {
        return isBlank(status) ? queryBuilder : queryBuilder.where().filter(new QueryFilter("STATUS", status));
    }

    private QueryBuilder addHeaderIdFilter(QueryBuilder queryBuilder, Long headerId) {
        return headerId == null ? queryBuilder : queryBuilder.and().filter(new QueryFilter("HEADER_ID", headerId));
    }

    private QueryBuilder addTradingDateFilter(QueryBuilder queryBuilder, Date tradingDate) {
        return tradingDate == null ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("TRADING_DATE", tradingDate));
    }

    private QueryBuilder addSellingMtnFilter(QueryBuilder queryBuilder, String sellingMtn) {
        return isBlank(sellingMtn) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("UPPER(SELLING_MTN)", "%" + sellingMtn.toUpperCase() + "%", LIKE));
    }

    private QueryBuilder addBillingIdFilter(QueryBuilder queryBuilder, String billingId) {
        return isBlank(billingId) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("UPPER(BILLING_ID)", "%" + billingId.toUpperCase() + "%", LIKE));
    }

    private QueryBuilder addSellingParticipantFilter(QueryBuilder queryBuilder, String sellingParticipant) {
        return isBlank(sellingParticipant) ? queryBuilder : queryBuilder
                .and().openParenthesis().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_NAME)",
                        "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                .or().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_SHORT_NAME)",
                        "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                .closeParenthesis();
    }

    private QueryBuilder addBuyingParticipantFilter(QueryBuilder queryBuilder, String buyingParticipant) {
        return isBlank(buyingParticipant) ? queryBuilder : queryBuilder
                .and().openParenthesis().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_NAME)",
                        "%" + buyingParticipant.toUpperCase() + "%", LIKE))
                .or().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                        "%" + buyingParticipant.toUpperCase() + "%", LIKE))
                .closeParenthesis();
    }

    private QueryBuilder addExpiredFilter(QueryBuilder queryBuilder, boolean expired) {
        return expired ? queryBuilder.and().filter(new QueryFilter("DEADLINE_DATE", new Date(), LESS_THAN_EQUALS))
                : queryBuilder;
    }

    private String getValue(Map<String, String> map, String key) {
        return getValue(map, key, String.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(Map<String, String> map, String key, Class returnType) {
        if (map.get(key) == null) {
            return null;
        }

        Object retVal;
        if (returnType == Long.class) {
            retVal = parseLong(map.get(key));
        } else if (returnType == Date.class) {
            retVal = parseDate(map.get(key));
        } else {
            retVal = map.get(key);
        }

        return (T) retVal;
    }

}

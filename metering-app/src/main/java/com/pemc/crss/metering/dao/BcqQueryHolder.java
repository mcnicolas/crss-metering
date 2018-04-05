package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageOrder;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dao.query.ComparisonOperator;
import com.pemc.crss.metering.dao.query.QueryData;
import com.pemc.crss.metering.dao.query.QueryFilter;
import com.pemc.crss.metering.dao.query.SelectQueryBuilder;
import com.pemc.crss.metering.dto.bcq.BcqHeader;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqStatus.getExcludedStatuses;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static com.pemc.crss.metering.utils.DateTimeUtils.now;
import static java.lang.Long.parseLong;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

public final class BcqQueryHolder {

    private static final String HEADER_JOIN_FILE = "TXN_BCQ_HEADER A"
            + " INNER JOIN TXN_BCQ_UPLOAD_FILE B ON A.FILE_ID = B.FILE_ID";

    private static final String EVENT_JOIN = "TXN_BCQ_SPECIAL_EVENT SE"
            + " INNER JOIN TXN_BCQ_EVENT_PARTICIPANT EP ON SE.EVENT_ID = EP.EVENT_ID"
            + " INNER JOIN TXN_BCQ_EVENT_TRADING_DATE ETD ON SE.EVENT_ID = ETD.EVENT_ID";

    public static QueryData headerPage(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        String status = getValue(mapParams, "status");
        boolean isSettlement = getValue(mapParams, "isSettlement") != null;
        QueryData uniqueHeaderIds = uniqueHeaderIds(mapParams);
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .column("HEADER_ID")
                .column("SELLING_MTN")
                .column("BILLING_ID")
                .column("TO_CHAR(TRADING_DATE, 'YYYY-MM-DD')").as("TRADING_DATE")
                .column("SELLING_PARTICIPANT_NAME")
                .column("SELLING_PARTICIPANT_SHORT_NAME")
                .column("BUYING_PARTICIPANT_NAME")
                .column("BUYING_PARTICIPANT_SHORT_NAME")
                .column(subHeaderPage("STRING_AGG(D.TRANSACTION_ID, ', ' ORDER BY D.SUBMITTED_DATE)", status,
                        isSettlement))
                .as("TRANSACTION_ID")
                .column(subHeaderPage("STRING_AGG(TO_CHAR(D.SUBMITTED_DATE, 'YYYY-MM-DD hh:MI AM'), ', ' "
                        + "ORDER BY D.SUBMITTED_DATE)", status, isSettlement))
                .as("SUBMITTED_DATE")
                .column(subHeaderPage("STRING_AGG(COALESCE(TO_CHAR(C.DEADLINE_DATE, 'YYYY-MM-DD'), ' '), ', ' "
                        + "ORDER BY D.SUBMITTED_DATE)", status, isSettlement))
                .as("DEADLINE_DATE")
                .column(subHeaderPage("STRING_AGG(C.STATUS, ', ' ORDER BY D.SUBMITTED_DATE)", status,
                        isSettlement))
                .as("STATUS")
                .column(subHeaderPage("STRING_AGG(COALESCE(C.UPDATED_VIA, ' '), ', ' ORDER BY D.SUBMITTED_DATE)",
                        status, isSettlement))
                .as("UPDATED_VIA")
                .from(HEADER_JOIN_FILE)
                .where().filter("HEADER_ID IN(" + uniqueHeaderIds.getSql() + ")")
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageableRequest.getPageNo(), pageableRequest.getPageSize());
        return queryBuilder.withSource(uniqueHeaderIds.getSource()).build();
    }

    public static QueryData headerPageCount(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        QueryData uniqueHeaderId = uniqueHeaderIds(mapParams);
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .count()
                .from(HEADER_JOIN_FILE)
                .where().filter("HEADER_ID IN(" + uniqueHeaderId.getSql() + ")");
        return queryBuilder.withSource(uniqueHeaderId.getSource()).build();
    }

    public static QueryData headerList(Map<String, String> mapParams) {
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .column("HEADER_ID")
                .column("SELLING_MTN")
                .column("BILLING_ID")
                .column("BUYING_PARTICIPANT_NAME")
                .column("BUYING_PARTICIPANT_SHORT_NAME")
                .column("SELLING_PARTICIPANT_NAME")
                .column("SELLING_PARTICIPANT_SHORT_NAME")
                .column("TRADING_DATE")
                .column("DEADLINE_DATE")
                .column("UPDATED_VIA")
                .column("STATUS")
                .column("TRANSACTION_ID")
                .column("SUBMITTED_DATE")
                .column("UPLOADED_BY")
                .from(HEADER_JOIN_FILE);
        return addFilters(queryBuilder, mapParams).build();
    }

    public static QueryData headerById(long headerId) {
        return new SelectQueryBuilder()
                .column("HEADER_ID")
                .column("SELLING_MTN")
                .column("BILLING_ID")
                .column("BUYING_PARTICIPANT_NAME")
                .column("BUYING_PARTICIPANT_SHORT_NAME")
                .column("SELLING_PARTICIPANT_NAME")
                .column("SELLING_PARTICIPANT_SHORT_NAME")
                .column("TRADING_DATE")
                .column("DEADLINE_DATE")
                .column("UPDATED_VIA")
                .column("STATUS")
                .column("TRANSACTION_ID")
                .column("SUBMITTED_DATE")
                .from(HEADER_JOIN_FILE)
                .where().filter(new QueryFilter("HEADER_ID", headerId))
                .build();
    }

    public static QueryData sameHeaders(BcqHeader header, List<BcqStatus> statuses, ComparisonOperator operator) {
        return new SelectQueryBuilder()
                .column("HEADER_ID")
                .column("SUBMITTED_DATE")
                .column("STATUS")
                .from(HEADER_JOIN_FILE)
                .where().filter(new QueryFilter("UPPER(SELLING_MTN)", header.getSellingMtn().toUpperCase()))
                .and().filter(new QueryFilter("UPPER(BILLING_ID)", header.getBillingId().toUpperCase()))
                .and().filter(new QueryFilter("TRADING_DATE", header.getTradingDate()))
                .and().filter(new QueryFilter("STATUS", statuses.stream().map(Enum::toString).collect(toList()), operator))
                .orderBy("SUBMITTED_DATE", DESC)
                .build();
    }

    public static QueryData dataByHeaderId(long headerId) {
        return new SelectQueryBuilder()
                .column("REFERENCE_MTN")
                .column("START_TIME")
                .column("END_TIME")
                .column("BCQ")
                .column("BUYER_MTN")
                .from("TXN_BCQ_DATA")
                .where().filter(new QueryFilter("HEADER_ID", headerId))
                .build();
    }

    public static QueryData eventParticipantsByTradingDate(Date tradingDate) {
        return new SelectQueryBuilder()
                .column("TRADING_PARTICIPANT").as("SHORT_NAME")
                .column("PARTICIPANT_NAME")
                .from(EVENT_JOIN)
                .where().filter(new QueryFilter("ETD.TRADING_DATE", tradingDate))
                .and().filter(new QueryFilter("SE.DEADLINE_DATE", now(), GREATER_THAN))
                .build();
    }

    public static QueryData eventDeadlineDateByTradingDateAndParticipant(Date tradingDate, String shortName) {
        return new SelectQueryBuilder()
                .column("DEADLINE_DATE")
                .from(EVENT_JOIN)
                .where().filter(new QueryFilter("ETD.TRADING_DATE", tradingDate))
                .and().filter(new QueryFilter("UPPER(EP.TRADING_PARTICIPANT)", shortName.toUpperCase()))
                .and().filter(new QueryFilter("SE.DEADLINE_DATE", now(), GREATER_THAN))
                .build();
    }

    public static QueryData prohibitedPage(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .column("ID")
                .column("CREATED_BY")
                .column("TO_CHAR(CREATED_DATE, 'YYYY-MM-DD hh:MI AM')").as("CREATED_DATE")
                .column("TO_CHAR(EFFECTIVE_START_DATE, 'YYYY-MM-DD')").as("EFFECTIVE_START_DATE")
                .column("TO_CHAR(EFFECTIVE_END_DATE, 'YYYY-MM-DD')").as("EFFECTIVE_END_DATE")
                .column("SELLING_MTN")
                .column("BILLING_ID")
                .from("TXN_BCQ_PROHIBITED")
                .where().filter("ENABLED = TRUE");

        return addProhibitedFilters(queryBuilder, mapParams)
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageableRequest.getPageNo(), pageableRequest.getPageSize())
                .build();
    }

    public static QueryData prohibitedPageCount(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .count()
                .from("TXN_BCQ_PROHIBITED")
                .where().filter("ENABLED = TRUE");

        return addProhibitedFilters(queryBuilder, mapParams).build();
    }

    public static QueryData enabledProhibitedList() {
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .column("UPPER(SELLING_MTN)").as("SELLING_MTN")
                .column("UPPER(BILLING_ID)").as("BILLING_ID")
                .column("EFFECTIVE_START_DATE")
                .column("EFFECTIVE_END_DATE")
                .from("TXN_BCQ_PROHIBITED")
                .where().filter("ENABLED = TRUE");

        return queryBuilder.build();
    }

    private static QueryData uniqueHeaderIds(Map<String, String> mapParams) {
        List<PageOrder> pageOrders = asList(
                new PageOrder("SELLING_MTN", ASC),
                new PageOrder("BILLING_ID", ASC),
                new PageOrder("TRADING_DATE", ASC),
                new PageOrder("HEADER_ID", DESC));
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .column("DISTINCT ON (SELLING_MTN, BILLING_ID, TRADING_DATE) HEADER_ID")
                .from(HEADER_JOIN_FILE);

        return addFilters(queryBuilder, mapParams)
                .orderBy(pageOrders)
                .build();
    }

    private static String subHeaderPage(String query, String status, boolean isSettlement) {
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder()
                .column(query)
                .from("TXN_BCQ_HEADER C INNER JOIN TXN_BCQ_UPLOAD_FILE D ON C.FILE_ID = D.FILE_ID")
                .where().filter("A.SELLING_MTN = C.SELLING_MTN")
                .and().filter("A.SELLING_MTN = C.SELLING_MTN")
                .and().filter("A.BILLING_ID = C.BILLING_ID")
                .and().filter("A.TRADING_DATE = C.TRADING_DATE")
                .and();
        if (status == null) {
            List<String> statusList = getExcludedStatuses(isSettlement).stream().map(Enum::toString).collect(toList());
            queryBuilder = queryBuilder.filter(new QueryFilter("C.STATUS", statusList, NOT_IN));
        } else {
            queryBuilder = queryBuilder.filter(new QueryFilter("C.STATUS", status));
        }
        return "(" + queryBuilder.build().getSql() + ")";
    }

    private static SelectQueryBuilder addFilters(SelectQueryBuilder queryBuilder, Map<String, String> mapParams) {
        String status = getValue(mapParams, "status");

        if (status == null) {
            queryBuilder = addIsSettlementFilter(queryBuilder, getValue(mapParams, "isSettlement") != null);
        } else {
            if (!"ALL".equals(status.toUpperCase())) {
                queryBuilder = addStatusFilter(queryBuilder, getValue(mapParams, "status"));
            }
        }
        queryBuilder = addHeaderIdFilter(queryBuilder, getValue(mapParams, "headerId", Long.class));
        queryBuilder = addTradingDateFilter(queryBuilder, getValue(mapParams, "tradingDate", Date.class));
        queryBuilder = addSellingMtnFilter(queryBuilder, getValue(mapParams, "sellingMtn"));
        queryBuilder = addBillingIdFilter(queryBuilder, getValue(mapParams, "billingId"));
        queryBuilder = addSellingParticipantFilter(queryBuilder, getValue(mapParams, "sellingParticipant"));
        queryBuilder = addBuyingParticipantFilter(queryBuilder, getValue(mapParams, "buyingParticipant"));
        queryBuilder = addExpiredFilter(queryBuilder, getValue(mapParams, "expired") != null);
        queryBuilder = addParticipantFilter(queryBuilder,
                getValue(mapParams, "sellingParticipant"),
                getValue(mapParams, "buyingParticipant"),
                getValue(mapParams, "participant"));
        queryBuilder = addShortNameFilter(queryBuilder, getValue(mapParams, "shortName"));


        return queryBuilder;
    }

    private static SelectQueryBuilder addProhibitedFilters(SelectQueryBuilder queryBuilder, Map<String, String> mapParams) {
        queryBuilder = addSellingMtnFilter(queryBuilder, getValue(mapParams, "sellingMtn"));
        queryBuilder = addBillingIdFilter(queryBuilder, getValue(mapParams, "billingId"));
        queryBuilder = addCreatedByFilter(queryBuilder, getValue(mapParams, "createdBy"));
        return queryBuilder;
    }

    private static SelectQueryBuilder addIsSettlementFilter(SelectQueryBuilder queryBuilder, boolean isSettlement) {
        List<String> statusList = getExcludedStatuses(isSettlement).stream().map(Enum::toString).collect(toList());
        return queryBuilder.where().filter(new QueryFilter("STATUS", statusList, NOT_IN));
    }

    private static SelectQueryBuilder addStatusFilter(SelectQueryBuilder queryBuilder, String status) {
        if (isBlank(status)) {
            return queryBuilder;
        }

        return queryBuilder.where().filter(new QueryFilter("STATUS", status));
    }

    private static SelectQueryBuilder addHeaderIdFilter(SelectQueryBuilder queryBuilder, Long headerId) {
        return isNull(headerId) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("HEADER_ID", headerId));
    }

    private static SelectQueryBuilder addTradingDateFilter(SelectQueryBuilder queryBuilder, Date tradingDate) {
        return isNull(tradingDate) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("TRADING_DATE", tradingDate));
    }

    private static SelectQueryBuilder addSellingMtnFilter(SelectQueryBuilder queryBuilder, String sellingMtn) {
        return isBlank(sellingMtn) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("UPPER(SELLING_MTN)", "%" + sellingMtn.toUpperCase() + "%", LIKE));
    }

    private static SelectQueryBuilder addBillingIdFilter(SelectQueryBuilder queryBuilder, String billingId) {
        return isBlank(billingId) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("UPPER(BILLING_ID)", "%" + billingId.toUpperCase() + "%", LIKE));
    }

    private static SelectQueryBuilder addSellingParticipantFilter(SelectQueryBuilder queryBuilder, String sellingParticipant) {
        return isBlank(sellingParticipant) ? queryBuilder : queryBuilder
                .and().openParenthesis().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_NAME)",
                        "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                .or().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_SHORT_NAME)",
                        "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                .closeParenthesis();
    }

    private static SelectQueryBuilder addShortNameFilter(SelectQueryBuilder queryBuilder, String shortName) {
        return isBlank(shortName) ? queryBuilder : queryBuilder
                .and().openParenthesis().filter(new QueryFilter("SELLING_PARTICIPANT_SHORT_NAME",
                        shortName, EQUALS))
                .or().filter(new QueryFilter("BUYING_PARTICIPANT_SHORT_NAME",
                        shortName, EQUALS))
                .closeParenthesis();
    }

    private static SelectQueryBuilder addBuyingParticipantFilter(SelectQueryBuilder queryBuilder, String buyingParticipant) {
        return isBlank(buyingParticipant) ? queryBuilder : queryBuilder
                .and().openParenthesis().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_NAME)",
                        "%" + buyingParticipant.toUpperCase() + "%", LIKE))
                .or().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                        "%" + buyingParticipant.toUpperCase() + "%", LIKE))
                .closeParenthesis();
    }

    private static SelectQueryBuilder addParticipantFilter(SelectQueryBuilder queryBuilder,
                                                           String sellingParticipant,
                                                           String buyingParticipant,
                                                           String participant) {
        if (isBlank(participant) || (!isBlank(sellingParticipant) && !isBlank(buyingParticipant))) {
            return queryBuilder;
        } else {
            if (isBlank(sellingParticipant) && !isBlank(buyingParticipant)) {
                return queryBuilder
                        .and().openParenthesis().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_SHORT_NAME)",
                                "%" + participant.toUpperCase() + "%", LIKE))
                        .or().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_SHORT_NAME)",
                                "%" + participant.toUpperCase() + "%", LIKE))
                        .closeParenthesis();
            } else if (!isBlank(sellingParticipant) && isBlank(buyingParticipant)) {
                return queryBuilder
                        .and().openParenthesis().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                                "%" + participant.toUpperCase() + "%", LIKE))
                        .or().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                                "%" + participant.toUpperCase() + "%", LIKE))
                        .closeParenthesis();
            } else {
                return queryBuilder
                        .and().openParenthesis().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_SHORT_NAME)",
                                "%" + participant.toUpperCase() + "%", LIKE))
                        .or().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                                "%" + participant.toUpperCase() + "%", LIKE))
                        .closeParenthesis();
            }
        }

    }

    private static SelectQueryBuilder addExpiredFilter(SelectQueryBuilder queryBuilder, boolean expired) {
        return !expired ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("DEADLINE_DATE", new Date(), LESS_THAN_EQUALS));
    }

    private static SelectQueryBuilder addEnabledFilter(SelectQueryBuilder queryBuilder) {
        return queryBuilder.where().filter("ENABLED = 1");
    }

    private static SelectQueryBuilder addCreatedByFilter(SelectQueryBuilder queryBuilder, String createdBy) {
        return isBlank(createdBy) ? queryBuilder : queryBuilder.and()
                .filter(new QueryFilter("UPPER(CREATED_BY)", "%" + createdBy.toUpperCase() + "%", LIKE));
    }

    private static String getValue(Map<String, String> map, String key) {
        return getValue(map, key, String.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(Map<String, String> map, String key, Class returnType) {
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

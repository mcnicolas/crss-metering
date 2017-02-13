package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.reports.ReportBean;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.constants.BcqUpdateType;
import com.pemc.crss.metering.dao.exception.InvalidStateException;
import com.pemc.crss.metering.dao.query.ComparisonOperator;
import com.pemc.crss.metering.dao.query.QueryBuilder;
import com.pemc.crss.metering.dao.query.QueryData;
import com.pemc.crss.metering.dao.query.QueryFilter;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqHeaderDisplay2;
import com.pemc.crss.metering.dto.bcq.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.mapper.BcqDataReportMapper;
import com.pemc.crss.metering.dto.bcq.mapper.BcqSpecialEventMapper;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqEventValidationData;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant;
import com.pemc.crss.metering.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.BcqStatus.CANCELLED;
import static com.pemc.crss.metering.constants.BcqStatus.CONFIRMED;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_APPROVAL_NEW;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_APPROVAL_UPDATED;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_CONFIRMATION;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_NULLIFICATION;
import static com.pemc.crss.metering.constants.BcqStatus.NULLIFIED;
import static com.pemc.crss.metering.constants.BcqStatus.SETTLEMENT_READY;
import static com.pemc.crss.metering.constants.BcqStatus.VOID;
import static com.pemc.crss.metering.constants.BcqStatus.fromString;
import static com.pemc.crss.metering.constants.BcqUpdateType.MANUAL_OVERRIDE;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.IN;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.LESS_THAN_EQUALS;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.LIKE;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.NOT_IN;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.sql.Types.VARCHAR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JdbcBcqDao implements BcqDao {

    @Value("${bcq.file.insert}")
    private String insertFile;

    @Value("${bcq.header.insert}")
    private String insertHeader;

    @Value("${bcq.header.status.update}")
    private String updateHeaderStatus;

    @Value("${bcq.header.status.update-settlement}")
    private String updateHeaderStatusBySettlement;

    @Value("${bcq.header.list.header-join-file}")
    private String headerJoinFile;

    @Value("${bcq.header.list.sub-select.transaction-id}")
    private String subSelectTransactionId;

    @Value("${bcq.header.list.sub-select.submitted-date}")
    private String subSelectSubmittedDate;

    @Value("${bcq.header.list.sub-select.deadline-date}")
    private String subSelectDeadlineDate;

    @Value("${bcq.header.list.sub-select.status}")
    private String subSelectStatus;

    @Value("${bcq.header.list.sub-select.updated-via}")
    private String subSelectUpdatedVia;

    @Value("${bcq.header.list.unique}")
    private String uniqueHeader;

    @Value("${bcq.data.insert}")
    private String insertData;

    @Value("${bcq.event.insert}")
    private String insertEvent;

    @Value("${bcq.event.trading-date.insert}")
    private String insertEventTradingDate;

    @Value("${bcq.event.participant.insert}")
    private String insertEventParticipant;

    @Value("${bcq.event.validate}")
    private String validateSpecialEvent;

    @Value("${bcq.event.list}")
    private String bcqEventList;

    @Value("${bcq.report.flattened}")
    private String bcqReportFlattened;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final CacheManager cacheManager;

    @Override
    public long saveUploadFile(BcqUploadFile uploadFile) {
        log.debug("[DAO-BCQ] Saving file: {}", uploadFile.getFileName());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(uploadFile);
        source.registerSqlType("validationStatus", VARCHAR);
        namedParameterJdbcTemplate.update(insertFile, source, keyHolder, new String[]{"file_id"});
        long id = keyHolder.getKey().longValue();
        log.debug("[DAO-BCQ] Saved file: {} with ID: {}", uploadFile.getFileName(), id);
        return id;
    }

    @Override
    public List<BcqHeader> saveHeaderList(List<BcqHeader> headerList, boolean isSpecialEvent) {
        List<BcqHeader> savedHeaderList = new ArrayList<>();
        for (BcqHeader header: headerList) {
            long headerId = saveHeader(header, isSpecialEvent);
            List<BcqData> dataList = header.getDataList();
            BeanPropertySqlParameterSource[] sourceArray = new BeanPropertySqlParameterSource[dataList.size()];
            for (int i = 0; i < dataList.size(); i ++) {
                BcqData data = dataList.get(i);
                data.setHeaderId(headerId);
                sourceArray[i] = new BeanPropertySqlParameterSource(data);
            }
            namedParameterJdbcTemplate.batchUpdate(insertData, sourceArray);
            header.setHeaderId(headerId);
            savedHeaderList.add(header);
        }
        return savedHeaderList;
    }

    @Override
    public Page<BcqHeaderDisplay2> findAllHeaders(PageableRequest pageableRequest) {
        int totalRecords = getTotalRecords(pageableRequest);
        Map<String, String> mapParams = pageableRequest.getMapParams();
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
                    .column(subSelectTransactionId).as("TRANSACTION_ID")
                    .column(subSelectSubmittedDate).as("SUBMITTED_DATE")
                    .column(subSelectDeadlineDate).as("DEADLINE_DATE")
                    .column(subSelectStatus).as("STATUS")
                    .column(subSelectUpdatedVia).as("UPDATED_VIA")
                .from(headerJoinFile)
                .where().filter(uniqueHeader);
        QueryData queryData = addParams(queryBuilder, mapParams, false)
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageableRequest.getPageNo(), pageableRequest.getPageSize())
                .build();
        log.debug("[DAO-BCQ] Finding page of headers with query: {}, and args: {}",
                queryData.getSql(), queryData.getSource());
        List<BcqHeaderDisplay2> headerList = namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BeanPropertyRowMapper<>(BcqHeaderDisplay2.class));
        log.debug("[DAO-BCQ] Found {} headers", headerList.size());
        return new PageImpl<>(headerList, pageableRequest.getPageable(), totalRecords);
    }

    @Override
    public List<BcqHeader> findAllHeaders(Map<String, String> mapParams) {
        QueryBuilder queryBuilder = new QueryBuilder()
                .select()
                    .column("HEADER_ID")
                    .column("SELLING_MTN")
                    .column("BILLING_ID")
                    .column("BUYING_PARTICIPANT_USER_ID")
                    .column("BUYING_PARTICIPANT_NAME")
                    .column("BUYING_PARTICIPANT_SHORT_NAME")
                    .column("SELLING_PARTICIPANT_USER_ID")
                    .column("SELLING_PARTICIPANT_NAME")
                    .column("SELLING_PARTICIPANT_SHORT_NAME")
                    .column("TRADING_DATE")
                    .column("DEADLINE_DATE")
                    .column("UPDATED_VIA")
                    .column("STATUS")
                    .column("TRANSACTION_ID")
                    .column("SUBMITTED_DATE")
                .from(headerJoinFile);
        QueryData queryData = addParams(queryBuilder, mapParams, false).build();
        log.debug("[DAO-BCQ] Finding all headers with query: {}, and args: {}", queryData.getSql(),
                queryData.getSource());
        List<BcqHeader> headerList = namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BcqHeaderRowMapper());
        log.debug("[DAO-BCQ] Found {} headers", headerList.size());
        return headerList;
    }

    @Override
    public List<BcqHeader> findSameHeadersWithStatusIn(BcqHeader header, List<BcqStatus> statuses) {
        return getSameHeadersWithStatus(header, statuses, IN);
    }

    @Override
    public List<BcqHeader> findSameHeadersWithStatusNotIn(BcqHeader header, List<BcqStatus> statuses) {
        return getSameHeadersWithStatus(header, statuses, NOT_IN);
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        log.debug("[DAO-BCQ] Finding header with ID: {}", headerId);
        QueryData queryData = new QueryBuilder()
                .select()
                    .column("HEADER_ID")
                    .column("SELLING_MTN")
                    .column("BILLING_ID")
                    .column("BUYING_PARTICIPANT_USER_ID")
                    .column("BUYING_PARTICIPANT_NAME")
                    .column("BUYING_PARTICIPANT_SHORT_NAME")
                    .column("SELLING_PARTICIPANT_USER_ID")
                    .column("SELLING_PARTICIPANT_NAME")
                    .column("SELLING_PARTICIPANT_SHORT_NAME")
                    .column("TRADING_DATE")
                    .column("DEADLINE_DATE")
                    .column("UPDATED_VIA")
                    .column("STATUS")
                    .column("TRANSACTION_ID")
                    .column("SUBMITTED_DATE")
                .from(headerJoinFile)
                .where()
                    .filter(new QueryFilter("HEADER_ID", headerId))
                .build();
        List<BcqHeader> headerList = namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BcqHeaderRowMapper());
        if (headerList.size() > 0) {
            BcqHeader header = headerList.get(0);
            log.debug("[DAO-BCQ] Found header: {}", header);
            return header;
        } else {
            log.debug("[DAO-BCQ] No header found with ID: {}", headerId);
            return null;
        }
    }

    @Override
    public List<BcqData> findDataByHeaderId(long headerId) {
        log.debug("[DAO-BCQ] Finding data of header with ID: {}", headerId);
        QueryData queryData = new QueryBuilder()
                .select()
                    .column("REFERENCE_MTN")
                    .column("END_TIME")
                    .column("BCQ")
                .from("TXN_BCQ_DATA")
                .where()
                    .filter(new QueryFilter("HEADER_ID", headerId))
                .build();
        List<BcqData> dataList = namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BeanPropertyRowMapper<>(BcqData.class));
        log.debug("[DAO-BCQ] Found {} data of header with ID: {}", dataList.size(), headerId);
        return dataList;
    }

    @Override
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        log.debug("[DAO-BCQ] Updating status of header to {} with ID: {}", status, headerId);
        BcqHeader header = findHeader(headerId);
        validateUpdateStatus(header.getStatus(), status);
        if (status == CONFIRMED || status == SETTLEMENT_READY) {
            List<BcqStatus> statusToCheck;
            if (status == CONFIRMED) {
                statusToCheck = singletonList(CONFIRMED);
            } else {
                statusToCheck = asList(CONFIRMED, SETTLEMENT_READY);
            }
            List<BcqHeader> prevHeaders = findSameHeadersWithStatusIn(header, statusToCheck);
            BcqHeader prevHeader = prevHeaders.size() > 0 ? prevHeaders.get(0) : null;
            updateHeaderStatusToVoid(prevHeader);
        }
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("headerId", headerId)
                .addValue("status", status.toString());
        namedParameterJdbcTemplate.update(updateHeaderStatus, source);
        log.debug("[DAO-BCQ] Updated status of header with ID: {} to {} ", headerId, status);
    }

    @Override
    public void updateHeaderStatusBySettlement(long headerId, BcqStatus status) {
        log.debug("[DAO-BCQ] Updating status by settlement of header to {} with ID: {}", status, headerId);
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("headerId", headerId)
                .addValue("status", status.toString())
                .addValue("updatedVia", MANUAL_OVERRIDE.toString());
        namedParameterJdbcTemplate.update(updateHeaderStatusBySettlement, source);
        log.debug("[DAO-BCQ] Updated status by settlement of header with ID: {} to {} ", headerId, status);
    }

    @Override
    public List<BcqSpecialEventList> getAllSpecialEvents() {
        log.debug("[DAO-BCQ] Querying all special Events");
        return namedParameterJdbcTemplate.query(bcqEventList, new BcqSpecialEventMapper());
    }

    @Override
    public long saveSpecialEvent(BcqSpecialEvent specialEvent) {
        log.debug("[DAO-BCQ] Saving new special event");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("deadlineDate", DateTimeUtils.endOfDay(specialEvent.getDeadlineDate()))
                .addValue("remarks", specialEvent.getRemarks())
                .addValue("createdDate", DateTimeUtils.now());
        namedParameterJdbcTemplate.update(insertEvent, source, keyHolder, new String[]{"event_id"});
        long eventId = keyHolder.getKey().longValue();
        saveEventTradingDates(specialEvent.getTradingDates(), eventId);
        saveEventParticipants(specialEvent.getTradingParticipants(), eventId);
        log.debug("[DAO-BCQ] Saved special event with ID: {}", eventId);
        return eventId;
    }

    @Override
    public List<BcqEventValidationData> checkDuplicateParticipantTradingDates(List<String> tradingParticipants,
                                                                              List<Date> tradingDates) {
        List<Date> tradingDatesAtStartOfDay = tradingDates.stream().map(DateTimeUtils::startOfDay)
                .collect(Collectors.toList());

        MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("dateToday", DateTimeUtils.startOfDay(new Date()))
                .addValue("tradingParticipants", tradingParticipants)
                .addValue("tradingDates", tradingDatesAtStartOfDay);

        return namedParameterJdbcTemplate.query(validateSpecialEvent, paramSource,
                new BeanPropertyRowMapper<>(BcqEventValidationData.class));
    }

    @Override
    public List<BcqSpecialEventParticipant> findEventParticipantsByTradingDate(Date tradingDate) {
        log.debug("[DAO-BCQ] Finding event participants with trading date: {}", tradingDate);
        QueryData queryData = new QueryBuilder()
                .select()
                .column("TRADING_PARTICIPANT").as("SHORT_NAME")
                .column("PARTICIPANT_NAME")
                .from("TXN_BCQ_SPECIAL_EVENT SE"
                        + " INNER JOIN TXN_BCQ_EVENT_PARTICIPANT EP ON SE.EVENT_ID = EP.EVENT_ID"
                        + " INNER JOIN TXN_BCQ_EVENT_TRADING_DATE ETD ON SE.EVENT_ID = ETD.EVENT_ID")
                .where()
                .filter(new QueryFilter("ETD.TRADING_DATE", tradingDate))
                .build();
        log.debug("[DAO-BCQ] Finding event query: {}", queryData.getSql());
        return namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BeanPropertyRowMapper<>(BcqSpecialEventParticipant.class));
    }

    @Override
    public List<ReportBean> queryBcqDataReport(final Map<String, String> mapParams) {
        QueryBuilder builder = new QueryBuilder(bcqReportFlattened);
        QueryData queryData = addParams(builder, mapParams, true).build();

        log.debug("[BCQ Data Report] Querying sql: {} source: {}", queryData.getSql(), queryData.getSource().getValues());

        return namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(), new BcqDataReportMapper());
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private long saveHeader(BcqHeader header, boolean isSpecialEvent) {
        log.debug("[DAO-BCQ] Saving header: {}, {}, {}",
                header.getSellingMtn(), header.getBillingId(), header.getTradingDate());
        if (isSpecialEvent) {
            header.setDeadlineDate(findDeadlineDateByTradingDateAndParticipant(header.getTradingDate(),
                    header.getBuyingParticipantShortName()));
        } else {
            long tradingDateInMillis = header.getTradingDate().getTime();
            long deadlineConfigInSeconds = DAYS.toSeconds(getDeadlineConfig());
            header.setDeadlineDate(new Date(tradingDateInMillis + SECONDS.toMillis(deadlineConfigInSeconds - 1)));
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        List<BcqHeader> prevHeaders = findSameHeadersWithStatusIn(header,
                asList(FOR_NULLIFICATION, FOR_CONFIRMATION, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATED));
        BcqHeader prevHeader = prevHeaders.size() > 0 ? prevHeaders.get(0) : null;
        updateHeaderStatusToVoid(prevHeader);
        BeanPropertySqlParameterSource beanSource = new BeanPropertySqlParameterSource(header);
        beanSource.registerSqlType("status", VARCHAR);
        beanSource.registerSqlType("updatedVia", VARCHAR);
        namedParameterJdbcTemplate.update(insertHeader, beanSource, keyHolder, new String[]{"header_id"});
        return keyHolder.getKey().longValue();
    }

    private List<BcqHeader> getSameHeadersWithStatus(BcqHeader header, List<BcqStatus> statuses,
                                                     ComparisonOperator operator) {

        log.debug("[DAO-BCQ] Finding previous header of: {}", header);
        QueryData queryData = new QueryBuilder()
                .select()
                    .column("HEADER_ID")
                    .column("SUBMITTED_DATE")
                    .column("STATUS")
                .from(headerJoinFile)
                .where()
                    .filter(new QueryFilter("SELLING_MTN", header.getSellingMtn()))
                    .and()
                    .filter(new QueryFilter("BILLING_ID", header.getBillingId()))
                    .and()
                    .filter(new QueryFilter("TRADING_DATE", header.getTradingDate()))
                    .and()
                    .filter(new QueryFilter("STATUS", statuses.stream().map(Enum::toString).collect(toList()), operator))
                .orderBy("SUBMITTED_DATE", DESC)
                .build();
        log.debug("SQL: {}", queryData.getSql());
        return namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BcqHeaderRowMapper());
    }

    private void updateHeaderStatusToVoid(BcqHeader header) {
        if (header != null) {
            log.debug("[DAO-BCQ] Previous header: {}", header);
            MapSqlParameterSource mapSource = new MapSqlParameterSource()
                    .addValue("status", VOID.toString())
                    .addValue("headerId", header.getHeaderId());
            namedParameterJdbcTemplate.update(updateHeaderStatus, mapSource);
        }
    }

    private int getDeadlineConfig() {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper valueWrapper = configCache.get("BCQ_DECLARATION_DEADLINE");
        return valueWrapper == null ? 2 : parseInt(valueWrapper.get().toString()) + 1;
    }

    private int getTotalRecords(PageableRequest pageableRequest) {
        Map<String, String> mapParams = pageableRequest.getMapParams();
        QueryBuilder queryBuilder = new QueryBuilder()
                .select().count()
                .from(headerJoinFile)
                .where(uniqueHeader);
        QueryData queryData = addParams(queryBuilder, mapParams, false).build();
        return namedParameterJdbcTemplate.queryForObject(queryData.getSql(), queryData.getSource(), Integer.class);
    }

    private QueryBuilder addParams(QueryBuilder queryBuilder, Map<String, String> mapParams, boolean forReports) {
        Long headerId = mapParams.get("headerId") == null ? null : parseLong(mapParams.get("headerId"));
        Date tradingDate = mapParams.get("tradingDate") == null ? null : parseDate(mapParams.get("tradingDate"));
        String sellingMtn = mapParams.get("sellingMtn") == null ? "" : mapParams.get("sellingMtn");
        String billingId = mapParams.get("billingId") == null ? "" : mapParams.get("billingId");
        String sellingParticipant = mapParams.get("sellingParticipant") == null ? "" : mapParams.get("sellingParticipant");
        String buyingParticipant = mapParams.get("buyingParticipant") == null ? "" : mapParams.get("buyingParticipant");
        String status = mapParams.get("status") == null ? null : mapParams.get("status");
        boolean expired = mapParams.get("expired") != null;
        if (headerId != null) {
            queryBuilder = queryBuilder.and().filter(new QueryFilter("HEADER_ID", headerId));
        }
        if (tradingDate != null) {
            queryBuilder = queryBuilder.and().filter(new QueryFilter("TRADING_DATE", tradingDate));
        }
        if (isNotBlank(sellingMtn)) {
            queryBuilder = queryBuilder
                    .and().filter(new QueryFilter("UPPER(SELLING_MTN)", "%" + sellingMtn.toUpperCase() + "%", LIKE));
        }
        if (isNotBlank(billingId)) {
            String column = forReports ? "UPPER(BILLING_ID)" : "UPPER(A.BILLING_ID)";
            queryBuilder = queryBuilder
                    .and().filter(new QueryFilter(column, "%" + billingId.toUpperCase() + "%", LIKE));
        }
        if (isNotBlank(sellingParticipant)) {
            queryBuilder = queryBuilder
                    .and().openParenthesis().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_NAME)",
                            "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                    .or().filter(new QueryFilter("UPPER(SELLING_PARTICIPANT_SHORT_NAME)",
                            "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                    .closeParenthesis();
        }
        if (isNotBlank(buyingParticipant)) {
            queryBuilder = queryBuilder
                    .and().openParenthesis().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_NAME)",
                            "%" + buyingParticipant.toUpperCase() + "%", LIKE))
                    .or().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                            "%" + buyingParticipant.toUpperCase() + "%", LIKE))
                    .closeParenthesis();
        }
        if (expired) {
            queryBuilder = queryBuilder.and().filter(new QueryFilter("DEADLINE_DATE", new Date(), LESS_THAN_EQUALS));
        }
        if (isNotBlank(status)) {
            queryBuilder = queryBuilder.and().filter(new QueryFilter("STATUS", status));
        }
        return queryBuilder;
    }

    private void validateUpdateStatus(BcqStatus oldStatus, BcqStatus newStatus) {
        if (asList(VOID, NULLIFIED, CONFIRMED, CANCELLED).contains(oldStatus)) {
            String pastAction = "";
            String pastActor = "";
            String currentAction = "";
            switch (oldStatus) {
                case CANCELLED:
                    pastAction = "cancelled";
                    pastActor = "seller";
                    break;
                case CONFIRMED:
                    if (newStatus == CANCELLED) {
                        return;
                    }
                    pastAction = "confirmed";
                    pastActor = "buyer";
                    break;
                case NULLIFIED:
                    pastAction = "nullified";
                    pastActor = "buyer";
                    break;
                default:
                    break;
            }
            switch (newStatus) {
                case CANCELLED:
                    currentAction = "cancel";
                    break;
                case CONFIRMED:
                    currentAction = "confirm";
                    break;
                case NULLIFIED:
                    currentAction = "nullify";
                    break;
                default:
                    break;
            }

            if (oldStatus == VOID) {
                throw new InvalidStateException(format("Unable to %s BCQ. BCQ declaration has a new version.",
                        currentAction));
            } else {
                throw new InvalidStateException(format("Unable to %s BCQ. BCQ declaration has already been %s by the %s",
                        currentAction, pastAction, pastActor));
            }
        }
    }

    private void saveEventTradingDates(List<Date> tradingDateList, long eventId) {
        log.debug("[DAO-BCQ] Saving event trading date list with size of: {} and event id: {}",
                tradingDateList.size(), eventId);
        MapSqlParameterSource[] sourceArray = new MapSqlParameterSource[tradingDateList.size()];
        for (int i = 0; i < tradingDateList.size(); i ++) {
            sourceArray[i] = new MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("tradingDate", DateTimeUtils.startOfDay(tradingDateList.get(i)));
        }
        namedParameterJdbcTemplate.batchUpdate(insertEventTradingDate, sourceArray);
        log.debug("[DAO-BCQ] Saved event trading date list");
    }

    private void saveEventParticipants(List<BcqSpecialEventParticipant> participants, long eventId) {
        log.debug("[DAO-BCQ] Saving event participant list with size of: {} and event id: {}",
                participants.size(), eventId);
        MapSqlParameterSource[] sourceArray = new MapSqlParameterSource[participants.size()];
        for (int i = 0; i < participants.size(); i ++) {
            sourceArray[i] = new MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("participantName", participants.get(i).getParticipantName())
                    .addValue("shortName", participants.get(i).getShortName());
        }
        namedParameterJdbcTemplate.batchUpdate(insertEventParticipant, sourceArray);
        log.debug("[DAO-BCQ] Saved event participant list");
    }

    private Date findDeadlineDateByTradingDateAndParticipant(Date tradingDate, String shortName) {
        log.debug("[DAO-BCQ] Finding event deadline date of: {}", shortName);
        QueryData queryData = new QueryBuilder()
                .select()
                .column("DEADLINE_DATE")
                .from("TXN_BCQ_SPECIAL_EVENT SE"
                        + " INNER JOIN TXN_BCQ_EVENT_PARTICIPANT EP ON SE.EVENT_ID = EP.EVENT_ID"
                        + " INNER JOIN TXN_BCQ_EVENT_TRADING_DATE ETD ON SE.EVENT_ID = ETD.EVENT_ID")
                .where()
                .filter(new QueryFilter("ETD.TRADING_DATE", tradingDate))
                .and()
                .filter(new QueryFilter("EP.TRADING_PARTICIPANT", shortName))
                .build();
        log.debug("[DAO-BCQ] Finding event query: {}", queryData.getSql());
        return namedParameterJdbcTemplate.queryForObject(queryData.getSql(), queryData.getSource(), Date.class);
    }

    private class BcqHeaderRowMapper implements RowMapper<BcqHeader> {
        @Override
        public BcqHeader mapRow(ResultSet rs, int rowNum) throws SQLException {
            BcqHeader header = new BcqHeader();
            BcqUploadFile uploadFile = new BcqUploadFile();
            if (doesColumnExist("header_id", rs)) {
                header.setHeaderId(rs.getLong("header_id"));
            }
            if (doesColumnExist("selling_mtn", rs)) {
                header.setSellingMtn(rs.getString("selling_mtn"));
            }
            if (doesColumnExist("billing_id", rs)) {
                header.setBillingId(rs.getString("billing_id"));
            }
            if (doesColumnExist("buying_participant_user_id", rs)) {
                header.setBuyingParticipantUserId(rs.getLong("buying_participant_user_id"));
            }
            if (doesColumnExist("buying_participant_name", rs)) {
                header.setBuyingParticipantName(rs.getString("buying_participant_name"));
            }
            if (doesColumnExist("buying_participant_short_name", rs)) {
                header.setBuyingParticipantShortName(rs.getString("buying_participant_short_name"));
            }
            if (doesColumnExist("selling_participant_user_id", rs)) {
                header.setSellingParticipantUserId(rs.getLong("selling_participant_user_id"));
            }
            if (doesColumnExist("selling_participant_name", rs)) {
                header.setSellingParticipantName(rs.getString("selling_participant_name"));
            }
            if (doesColumnExist("selling_participant_short_name", rs)) {
                header.setSellingParticipantShortName(rs.getString("selling_participant_short_name"));
            }
            if (doesColumnExist("trading_date", rs)) {
                header.setTradingDate(rs.getDate("trading_date"));
            }
            if (doesColumnExist("deadline_date", rs)) {
                header.setDeadlineDate(rs.getTimestamp("deadline_date"));
            }
            if (doesColumnExist("updated_via", rs)) {
                if (rs.getString("updated_via") != null) {
                    header.setUpdatedVia(BcqUpdateType.fromString(rs.getString("updated_via")));
                }
            }
            if (doesColumnExist("status", rs)) {
                header.setStatus(fromString(rs.getString("status")));
            }
            if (doesColumnExist("transaction_id", rs)) {
                uploadFile.setTransactionId(rs.getString("transaction_id"));
            }
            if (doesColumnExist("submitted_date", rs)) {
                uploadFile.setSubmittedDate(rs.getTimestamp("submitted_date"));
            }
            header.setUploadFile(uploadFile);
            return header;
        }

        private boolean doesColumnExist(String columnName, ResultSet rs) throws SQLException{
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i ++) {
                if(meta.getColumnName(i).equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
            return false;
        }
    }
}

package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.constants.BcqUpdateType;
import com.pemc.crss.metering.dao.query.QueryBuilder;
import com.pemc.crss.metering.dao.query.QueryData;
import com.pemc.crss.metering.dao.query.QueryFilter;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqHeaderDisplay2;
import com.pemc.crss.metering.dto.bcq.BcqUploadFile;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.BcqUpdateType.MANUAL_OVERRIDE;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.sql.Types.VARCHAR;
import static java.util.Arrays.asList;
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
    public List<BcqHeader> saveHeaderList(List<BcqHeader> headerList) {
        List<BcqHeader> savedHeaderList = new ArrayList<>();
        for (BcqHeader header: headerList) {
            long headerId = saveHeader(header);
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
                    .column("TO_CHAR(TRADING_DATE, 'YYYY-DD-MM')").as("TRADING_DATE")
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
        QueryData queryData = addParams(queryBuilder, mapParams)
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
        QueryData queryData = addParams(queryBuilder, mapParams).build();
        log.debug("[DAO-BCQ] Finding all headers with query: {}, and args: {}", queryData.getSql(),
                queryData.getSource());
        List<BcqHeader> headerList = namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BcqHeaderRowMapper());
        log.debug("[DAO-BCQ] Found {} headers", headerList.size());
        return headerList;
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
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("headerId", headerId);
        source.addValue("status", status.toString());
        namedParameterJdbcTemplate.update(updateHeaderStatus, source);
        log.debug("[DAO-BCQ] Updated status of header with ID: {} to {} ", headerId, status);
    }

    @Override
    public void updateHeaderStatusBySettlement(long headerId, BcqStatus status) {
        log.debug("[DAO-BCQ] Updating status by settlement of header to {} with ID: {}", status, headerId);
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("headerId", headerId);
        source.addValue("status", status.toString());
        source.addValue("updatedVia", MANUAL_OVERRIDE.toString());
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
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("deadlineDate", specialEvent.getDeadlineDate());
        source.addValue("remarks", specialEvent.getRemarks());
        source.addValue("createdDate", DateTimeUtils.now());
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

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private long saveHeader(BcqHeader header) {
        log.debug("[DAO-BCQ] Saving header: {}, {}, {}",
                header.getSellingMtn(), header.getBillingId(), header.getTradingDate());
        long tradingDateInMillis = header.getTradingDate().getTime();
        long deadlineConfigInSeconds = DAYS.toSeconds(getDeadlineConfig());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        header.setDeadlineDate(new Date(tradingDateInMillis + SECONDS.toMillis(deadlineConfigInSeconds - 1)));
        List<BcqHeader> prevHeaders = getPrevHeadersWithStatusIn(header, asList(FOR_NULLIFICATION, FOR_CONFIRMATION));
        BcqHeader prevHeader = prevHeaders.size() > 0 ? prevHeaders.get(0) : null;
        if (prevHeader != null) {
            log.debug("[DAO-BCQ] Previous header: {}", prevHeader);
            MapSqlParameterSource mapSource = new MapSqlParameterSource();
            mapSource.addValue("status", VOID);
            mapSource.addValue("headerId", prevHeader.getHeaderId());
            mapSource.registerSqlType("status", VARCHAR);
            namedParameterJdbcTemplate.update(updateHeaderStatus, mapSource);
        }
        BeanPropertySqlParameterSource beanSource = new BeanPropertySqlParameterSource(header);
        beanSource.registerSqlType("status", VARCHAR);
        beanSource.registerSqlType("updatedVia", VARCHAR);
        namedParameterJdbcTemplate.update(insertHeader, beanSource, keyHolder, new String[]{"header_id"});
        return keyHolder.getKey().longValue();
    }

    private List<BcqHeader> getPrevHeadersWithStatusIn(BcqHeader header, List<BcqStatus> statuses) {
        QueryData queryData = new QueryBuilder()
                .select()
                    .column("HEADER_ID")
                    .column("STATUS")
                .from(headerJoinFile)
                .where()
                    .filter(new QueryFilter("SELLING_MTN", header.getSellingMtn()))
                    .and()
                    .filter(new QueryFilter("BILLING_ID", header.getBillingId()))
                    .and()
                    .filter(new QueryFilter("TRADING_DATE", header.getTradingDate()))
                    .and()
                    .filter(new QueryFilter("STATUS", statuses.stream().map(Enum::toString).collect(toList()), IN))
                .orderBy("SUBMITTED_DATE", DESC)
                .build();
        log.debug("SQL: {}", queryData.getSql());
        return namedParameterJdbcTemplate.query(queryData.getSql(), queryData.getSource(),
                new BeanPropertyRowMapper<>(BcqHeader.class));
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
        QueryData queryData = addParams(queryBuilder, mapParams).build();
        log.debug("QUERY: {}", queryData.getSql());
        return namedParameterJdbcTemplate.queryForObject(queryData.getSql(), queryData.getSource(), Integer.class);
    }

    private QueryBuilder addParams(QueryBuilder queryBuilder, Map<String, String> mapParams) {
        Long headerId = mapParams.get("headerId") == null ? null : parseLong(mapParams.get("headerId"));
        String sellingMtn = mapParams.get("sellingMtn") == null ? "" : mapParams.get("sellingMtn");
        String billingId = mapParams.get("billingId") == null ? "" : mapParams.get("billingId");
        String sellingParticipant = mapParams.get("sellingParticipant") == null ? "" : mapParams.get("sellingParticipant");
        String buyingParticipant = mapParams.get("buyingParticipant") == null ? "" : mapParams.get("buyingParticipant");
        Date tradingDate = parseDate(mapParams.get("tradingDate"));
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
            queryBuilder = queryBuilder
                    .and().filter(new QueryFilter("UPPER(A.BILLING_ID)", "%" + billingId.toUpperCase() + "%", LIKE));
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
                            "%" + sellingParticipant.toUpperCase() + "%", LIKE))
                    .or().filter(new QueryFilter("UPPER(BUYING_PARTICIPANT_SHORT_NAME)",
                            "%" + sellingParticipant.toUpperCase() + "%", LIKE))
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

    private void saveEventTradingDates(List<Date> tradingDateList, long eventId) {
        log.debug("[DAO-BCQ] Saving event trading date list with size of: {} and event id: {}",
                tradingDateList.size(), eventId);
        MapSqlParameterSource[] sourceArray = new MapSqlParameterSource[tradingDateList.size()];
        for (int i = 0; i < tradingDateList.size(); i ++) {
            sourceArray[i] = new MapSqlParameterSource();
            sourceArray[i].addValue("eventId", eventId);
            sourceArray[i].addValue("tradingDate", tradingDateList.get(i));
        }
        namedParameterJdbcTemplate.batchUpdate(insertEventTradingDate, sourceArray);
        log.debug("[DAO-BCQ] Saved event trading date list");
    }

    private void saveEventParticipants(List<BcqSpecialEventParticipant> participants, long eventId) {
        log.debug("[DAO-BCQ] Saving event participant list with size of: {} and event id: {}",
                participants.size(), eventId);
        MapSqlParameterSource[] sourceArray = new MapSqlParameterSource[participants.size()];
        for (int i = 0; i < participants.size(); i ++) {
            sourceArray[i] = new MapSqlParameterSource();
            sourceArray[i].addValue("eventId", eventId);
            sourceArray[i].addValue("participantName", participants.get(i).getParticipantName());
            sourceArray[i].addValue("shortName", participants.get(i).getShortName());
        }
        namedParameterJdbcTemplate.batchUpdate(insertEventParticipant, sourceArray);
        log.debug("[DAO-BCQ] Saved event participant list");
    }

    private class BcqHeaderRowMapper implements RowMapper<BcqHeader> {
        @Override
        public BcqHeader mapRow(ResultSet rs, int rowNum) throws SQLException {
            BcqHeader header = new BcqHeader();
            BcqUploadFile uploadFile = new BcqUploadFile();
            header.setHeaderId(rs.getLong("header_id"));
            header.setSellingMtn(rs.getString("selling_mtn"));
            header.setBillingId(rs.getString("billing_id"));
            header.setBuyingParticipantUserId(rs.getLong("buying_participant_user_id"));
            header.setBuyingParticipantName(rs.getString("buying_participant_name"));
            header.setBuyingParticipantShortName(rs.getString("buying_participant_short_name"));
            header.setSellingParticipantUserId(rs.getLong("selling_participant_user_id"));
            header.setSellingParticipantName(rs.getString("selling_participant_name"));
            header.setSellingParticipantShortName(rs.getString("selling_participant_short_name"));
            header.setTradingDate(rs.getDate("trading_date"));
            header.setDeadlineDate(rs.getTimestamp("deadline_date"));
            if (rs.getString("updated_via") != null) {
                header.setUpdatedVia(BcqUpdateType.fromString(rs.getString("updated_via")));
            }
            header.setStatus(fromString(rs.getString("status")));
            uploadFile.setTransactionId(rs.getString("transaction_id"));
            uploadFile.setSubmittedDate(rs.getTimestamp("submitted_date"));
            header.setUploadFile(uploadFile);
            return header;
        }
    }
}

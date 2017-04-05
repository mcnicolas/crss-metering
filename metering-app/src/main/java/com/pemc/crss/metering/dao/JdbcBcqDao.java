package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.commons.reports.ReportBean;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.constants.BcqUpdateType;
import com.pemc.crss.metering.dao.query.ComparisonOperator;
import com.pemc.crss.metering.dao.query.QueryData;
import com.pemc.crss.metering.dao.query.QueryFilter;
import com.pemc.crss.metering.dao.query.SelectQueryBuilder;
import com.pemc.crss.metering.dto.bcq.*;
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
import java.util.stream.IntStream;

import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.BcqUpdateType.MANUAL_OVERRIDE;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static java.lang.Long.parseLong;
import static java.sql.Types.VARCHAR;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

    @Value("${bcq.header.status.select-by-status-and-deadlinedate-plus-days}")
    private String selectByStatusAndDeadlineDatePlusDays;

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

    @Value("${bcq.prohibited.insert}")
    private String insertProhibited;

    @Value("${bcq.prohibited.disable}")
    private String disableProhibited;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CacheConfigService configService;

    @Override
    public long saveUploadFile(BcqUploadFile uploadFile) {
        log.debug("Saving file: {}", uploadFile.getFileName());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(uploadFile);
        source.registerSqlType("validationStatus", VARCHAR);
        jdbcTemplate.update(insertFile, source, keyHolder, new String[]{"file_id"});
        long fileId = keyHolder.getKey().longValue();
        log.debug("Saved file: {} with ID: {}", uploadFile.getFileName(), fileId);
        return fileId;
    }

    @Override
    public List<BcqHeader> saveHeaders(List<BcqHeader> headerList, boolean isSpecialEvent) {
        List<BcqHeader> savedHeaderList = new ArrayList<>();
        for (BcqHeader header: headerList) {
            long headerId = saveHeader(header, isSpecialEvent);
            List<BcqData> dataList = header.getDataList();
            BeanPropertySqlParameterSource[] sourceArray = new BeanPropertySqlParameterSource[dataList.size()];
            IntStream.range(0, dataList.size()).forEach(i -> {
                BcqData data = dataList.get(i);
                data.setHeaderId(headerId);
                sourceArray[i] = new BeanPropertySqlParameterSource(data);
            });
            jdbcTemplate.batchUpdate(insertData, sourceArray);
            header.setHeaderId(headerId);
            savedHeaderList.add(header);
        }
        return savedHeaderList;
    }

    @Override
    public Page<BcqHeaderPageDisplay> findAllHeaders(PageableRequest pageableRequest) {
        int totalRecords = getHeaderCount(pageableRequest);
        QueryData data = BcqQueryHolder.headerPage(pageableRequest);
        log.debug("Finding page of headers with query: {}, and args: {}", data.getSql(), data.getSource().getValues());
        List<BcqHeaderPageDisplay> headerList = jdbcTemplate.query(data.getSql(), data.getSource(),
                new BeanPropertyRowMapper<>(BcqHeaderPageDisplay.class));
        log.debug("Found {} headers", headerList.size());
        return new PageImpl<>(headerList, pageableRequest.getPageable(), totalRecords);
    }

    @Override
    public List<BcqHeader> findAllHeaders(Map<String, String> mapParams) {
        QueryData data = BcqQueryHolder.headerList(mapParams);
        log.debug("Finding list of headers with query: {}, and args: {}", data.getSql(), data.getSource().getValues());
        List<BcqHeader> headerList = jdbcTemplate.query(data.getSql(), data.getSource(), new BcqHeaderRowMapper());
        log.debug("Found {} headers", headerList.size());
        return headerList;
    }

    @Override
    public List<BcqHeader> findSameHeaders(BcqHeader header, List<BcqStatus> statuses, ComparisonOperator operator) {
        QueryData data = BcqQueryHolder.sameHeaders(header, statuses, operator);
        log.debug("Finding same headers of: {} with query: {}, and args: {}", header, data.getSql(),
                data.getSource().getValues());
        return jdbcTemplate.query(data.getSql(), data.getSource(), new BcqHeaderRowMapper());
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        log.debug("Finding header with ID: {}", headerId);
        QueryData data = BcqQueryHolder.headerById(headerId);
        List<BcqHeader> headerList = jdbcTemplate.query(data.getSql(), data.getSource(), new BcqHeaderRowMapper());
        if (headerList.size() > 0) {
            BcqHeader header = headerList.get(0);
            log.debug("Found header: {}", header);
            return header;
        }
        log.debug("No header found with ID: {}", headerId);
        return null;
    }

    @Override
    public List<BcqData> findDataByHeaderId(long headerId) {
        log.debug("Finding data of header with ID: {}", headerId);
        QueryData data = BcqQueryHolder.dataByHeaderId(headerId);
        List<BcqData> dataList = jdbcTemplate.query(data.getSql(), data.getSource(), new BeanPropertyRowMapper<>(BcqData.class));
        log.debug("Found {} data of header with ID: {}", dataList.size(), headerId);
        return dataList;
    }

    @Override
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("headerId", headerId)
                .addValue("status", status.toString());
        jdbcTemplate.update(updateHeaderStatus, source);
        log.debug("Updated status of header with ID: {} to {} ", headerId, status);
    }

    @Override
    public void updateHeaderStatusBySettlement(long headerId, BcqStatus status) {
        log.debug("Updating status by settlement of header to {} with ID: {}", status, headerId);
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("headerId", headerId)
                .addValue("status", status.toString())
                .addValue("updatedVia", MANUAL_OVERRIDE.toString());
        jdbcTemplate.update(updateHeaderStatusBySettlement, source);
        log.debug("Updated status by settlement of header with ID: {} to {} ", headerId, status);
    }

    @Override
    public List<BcqSpecialEventList> findAllSpecialEvents() {
        log.debug("Querying all special Events");
        return jdbcTemplate.query(bcqEventList, new BcqSpecialEventMapper());
    }

    @Override
    public long saveSpecialEvent(BcqSpecialEvent specialEvent) {
        log.debug("Saving new special event");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("deadlineDate", DateTimeUtils.endOfDay(specialEvent.getDeadlineDate()))
                .addValue("remarks", specialEvent.getRemarks());
        jdbcTemplate.update(insertEvent, source, keyHolder, new String[]{"event_id"});
        long eventId = keyHolder.getKey().longValue();
        saveEventTradingDates(specialEvent.getTradingDates(), eventId);
        saveEventParticipants(specialEvent.getTradingParticipants(), eventId);
        log.debug("Saved special event with ID: {}", eventId);
        return eventId;
    }

    @Override
    public List<BcqEventValidationData> checkDuplicateParticipantTradingDates(List<String> tradingParticipants,
                                                                              List<Date> tradingDates) {
        List<Date> tradingDatesAtStartOfDay = tradingDates.stream().map(DateTimeUtils::startOfDay)
                .collect(Collectors.toList());

        MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("dateToday", startOfDay(new Date()))
                .addValue("tradingParticipants", tradingParticipants)
                .addValue("tradingDates", tradingDatesAtStartOfDay);

        return jdbcTemplate.query(validateSpecialEvent, paramSource,
                new BeanPropertyRowMapper<>(BcqEventValidationData.class));
    }

    @Override
    public List<BcqSpecialEventParticipant> findEventParticipantsByTradingDate(Date tradingDate) {
        log.debug("Finding event participants with trading date: {}", tradingDate);
        QueryData data = BcqQueryHolder.eventParticipantsByTradingDate(tradingDate);
        log.debug("Finding event query: {}", data.getSql());
        return jdbcTemplate.query(data.getSql(), data.getSource(),
                new BeanPropertyRowMapper<>(BcqSpecialEventParticipant.class));
    }

    @Override
    public Date findEventDeadlineDateByTradingDateAndParticipant(Date tradingDate, String shortName) {
        log.debug("Finding event deadline date of: {}", shortName);
        QueryData data = BcqQueryHolder.eventDeadlineDateByTradingDateAndParticipant(tradingDate, shortName);
        log.debug("Finding event query: {}", data.getSql());
        List<Date> foundDeadlineDates = jdbcTemplate.queryForList(data.getSql(), data.getSource(), Date.class);
        if (foundDeadlineDates.size() > 1) {
            log.error("Deadline date must be only one");
        }
        return foundDeadlineDates.size() > 0 ? foundDeadlineDates.get(0) : null;
    }

    @Override
    public List<ReportBean> queryBcqDataReport(final Map<String, String> mapParams) {
        SelectQueryBuilder builder = new SelectQueryBuilder(bcqReportFlattened);
        QueryData queryData = addParams(builder, mapParams, true).build();

        log.debug("[BCQ Data Report] Querying sql: {} source: {}", queryData.getSql(), queryData.getSource().getValues());

        return jdbcTemplate.query(queryData.getSql(), queryData.getSource(), new BcqDataReportMapper());
    }

    @Override
    public List<Long> selectByStatusAndDeadlineDatePlusDays(BcqStatus status, Integer plusDays) {
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("plusDays", plusDays)
                .addValue("status", status.toString());

        return jdbcTemplate.queryForList(selectByStatusAndDeadlineDatePlusDays,
                source, Long.class);
    }

    @Override
    public Page<BcqProhibitedPairPageDisplay> findAllProhibitedPairs(PageableRequest pageableRequest) {
        int totalRecords = getProhibitedCount(pageableRequest);
        QueryData data = BcqQueryHolder.prohibitedPage(pageableRequest);
        log.debug("Finding page of prohibited with query: {}, and args: {}", data.getSql(), data.getSource().getValues());
        List<BcqProhibitedPairPageDisplay> prohibitedList = jdbcTemplate.query(data.getSql(), data.getSource(),
                new BeanPropertyRowMapper<>(BcqProhibitedPairPageDisplay.class));
        log.debug("Found {} prohibited", prohibitedList.size());
        return new PageImpl<>(prohibitedList, pageableRequest.getPageable(), totalRecords);
    }

    @Override
    public long saveProhibitedPair(BcqProhibitedPair prohibitedPair) {
        log.debug("Saving prohibited pair: {}", prohibitedPair);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(prohibitedPair);
        jdbcTemplate.update(insertProhibited, source, keyHolder, new String[]{"id"});
        long id = keyHolder.getKey().longValue();
        log.debug("Saved prohibited pair with ID: {}", id);
        return id;
    }

    @Override
    public void disableProhibitedPair(long id) {
        log.debug("Disabling prohibited pair with ID: {}", id);
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(disableProhibited, source);
        log.debug("Disabled prohibited pair with ID: {}", id);
    }

    @Override
    public List<BcqProhibitedPair> findAllEnabledProhibitedPairs() {
        log.debug("Getting all enabled prohibited pairs");
        QueryData data = BcqQueryHolder.enabledProhibitedList();
        List<BcqProhibitedPair> prohibitedPairs = jdbcTemplate.query(data.getSql(),
                new BeanPropertyRowMapper<>(BcqProhibitedPair.class));
        log.debug("Found {} enabled prohibited", prohibitedPairs.size());
        return prohibitedPairs;
    }

    private long saveHeader(BcqHeader header, boolean isSpecialEvent) {
        log.debug("Saving header: {}", header);
        if (header.getUpdatedVia() == MANUAL_OVERRIDE) {
            header.setDeadlineDate(null);
        } else {
            if (isSpecialEvent) {
                header.setDeadlineDate(findEventDeadlineDateByTradingDateAndParticipant(header.getTradingDate(),
                        header.getBuyingParticipantShortName()));
            } else {
                int deadlineDays = configService.getIntegerValueForKey("BCQ_DECLARATION_DEADLINE", 1) + 1;
                long tradingDateInMillis = header.getTradingDate().getTime();
                long deadlineConfigInSeconds = DAYS.toSeconds(deadlineDays);
                header.setDeadlineDate(new Date(tradingDateInMillis + SECONDS.toMillis(deadlineConfigInSeconds - 1)));
            }
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        List<BcqHeader> sameHeaders = findSameHeaders(header, FOR_STATUSES, IN);
        if (sameHeaders.size() > 0) {
            BcqHeader prevHeader = sameHeaders.get(0);
            updateHeaderStatus(prevHeader.getHeaderId(), VOID);
        }
        BcqHeader prevHeader = sameHeaders.size() > 0 ? sameHeaders.get(0) : null;
        updateHeaderStatusToVoid(prevHeader);
        BeanPropertySqlParameterSource beanSource = new BeanPropertySqlParameterSource(header);
        beanSource.registerSqlType("status", VARCHAR);
        beanSource.registerSqlType("updatedVia", VARCHAR);
        jdbcTemplate.update(insertHeader, beanSource, keyHolder, new String[]{"header_id"});
        return keyHolder.getKey().longValue();
    }

    private void updateHeaderStatusToVoid(BcqHeader header) {
        if (header != null) {
            log.debug("Previous header: {}", header);
            MapSqlParameterSource mapSource = new MapSqlParameterSource()
                    .addValue("status", VOID.toString())
                    .addValue("headerId", header.getHeaderId());
            jdbcTemplate.update(updateHeaderStatus, mapSource);
        }
    }

    private int getHeaderCount(PageableRequest pageableRequest) {
        log.debug("Getting total declaration records");
        QueryData queryData = BcqQueryHolder.headerPageCount(pageableRequest);
        int totalRecords = jdbcTemplate.queryForObject(queryData.getSql(), queryData.getSource(), Integer.class);
        log.debug("Total records: {}", totalRecords);
        return totalRecords;
    }

    private SelectQueryBuilder addParams(SelectQueryBuilder queryBuilder, Map<String, String> mapParams, boolean forReports) {
        Long headerId = mapParams.get("headerId") == null ? null : parseLong(mapParams.get("headerId"));
        Date tradingDate = mapParams.get("tradingDate") == null ? null : parseDate(mapParams.get("tradingDate"));
        String sellingMtn = mapParams.get("sellingMtn") == null ? "" : mapParams.get("sellingMtn");
        String billingId = mapParams.get("billingId") == null ? "" : mapParams.get("billingId");
        String sellingParticipant = mapParams.get("sellingParticipant") == null ? "" : mapParams.get("sellingParticipant");
        String buyingParticipant = mapParams.get("buyingParticipant") == null ? "" : mapParams.get("buyingParticipant");
        String status = mapParams.get("status");
        boolean expired = mapParams.get("expired") != null;
        boolean isSettlement = mapParams.get("isSettlement") != null;
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
        } else {
            List<String> statusList;
            if (isSettlement) {
                statusList = of(VOID).map(Enum::toString).collect(toList());
            } else {
                statusList = of(VOID, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE, FOR_APPROVAL_CANCEL).map(Enum::toString)
                        .collect(toList());
            }
            queryBuilder = queryBuilder.and().filter(new QueryFilter("STATUS", statusList, NOT_IN));
        }
        return queryBuilder;
    }

    private void saveEventTradingDates(List<Date> tradingDateList, long eventId) {
        log.debug("Saving event trading date list with size of: {} and event id: {}",
                tradingDateList.size(), eventId);
        MapSqlParameterSource[] sourceArray = new MapSqlParameterSource[tradingDateList.size()];
        for (int i = 0; i < tradingDateList.size(); i ++) {
            sourceArray[i] = new MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("tradingDate", startOfDay(tradingDateList.get(i)));
        }
        jdbcTemplate.batchUpdate(insertEventTradingDate, sourceArray);
        log.debug("Saved event trading date list");
    }

    private void saveEventParticipants(List<BcqSpecialEventParticipant> participants, long eventId) {
        log.debug("Saving event participant list with size of: {} and event id: {}",
                participants.size(), eventId);
        MapSqlParameterSource[] sourceArray = new MapSqlParameterSource[participants.size()];
        for (int i = 0; i < participants.size(); i ++) {
            sourceArray[i] = new MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("participantName", participants.get(i).getParticipantName())
                    .addValue("shortName", participants.get(i).getShortName());
        }
        jdbcTemplate.batchUpdate(insertEventParticipant, sourceArray);
        log.debug("Saved event participant list");
    }

    private int getProhibitedCount(PageableRequest pageableRequest) {
        log.debug("Getting count of prohibited");
        QueryData queryData = BcqQueryHolder.prohibitedPageCount(pageableRequest);
        int totalRecords = jdbcTemplate.queryForObject(queryData.getSql(), queryData.getSource(), Integer.class);
        log.debug("Count: {}", totalRecords);
        return totalRecords;
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
            if (doesColumnExist("buying_participant_name", rs)) {
                header.setBuyingParticipantName(rs.getString("buying_participant_name"));
            }
            if (doesColumnExist("buying_participant_short_name", rs)) {
                header.setBuyingParticipantShortName(rs.getString("buying_participant_short_name"));
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

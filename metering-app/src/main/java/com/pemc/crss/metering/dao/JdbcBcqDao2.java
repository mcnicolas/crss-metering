package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqStatus.fromString;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JdbcBcqDao2 implements BcqDao2 {

    @Value("${bcq.manifest}")
    private String insertManifest;

    @Value("${bcq.header.insert}")
    private String insertHeader;

    @Value("${bcq.header.update}")
    private String updateHeader;

    @Value("${bcq.header.status}")
    private String updateHeaderStatus;

    @Value("${bcq.data.insert}")
    private String insertData;

    @Value("${bcq.data.update}")
    private String updateData;

    @Value("${bcq.data.details}")
    private String dataDetails;

    @Value("${bcq.display.data}")
    private String displayData;

    @Value("${bcq.display.count}")
    private String displayCount;

    @Value("${bcq.display.paginate}")
    private String displayPaginate;

    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;

    @Override
    public long saveUploadFile(BcqUploadFile uploadFile) {
        log.debug("[DAO-BCQ] Saving file: {}", uploadFile.getFileName());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertManifest, new String[]{"file_id"});
                    ps.setString(1, uploadFile.getTransactionId());
                    ps.setString(2, uploadFile.getFileName());
                    ps.setLong(3, uploadFile.getFileSize());
                    ps.setTimestamp(4, new Timestamp(uploadFile.getSubmittedDate().getTime()));
                    ps.setString(5, uploadFile.getValidationStatus().toString());
                    return ps;
                },
                keyHolder);
        long id = keyHolder.getKey().longValue();
        log.debug("[DAO-BCQ] Saved file: {} with ID: {}", uploadFile.getFileName(), id);
        return keyHolder.getKey().longValue();
    }

    @Override
    public List<BcqHeader> saveHeaderList(List<BcqHeader> headerList) {
        List<BcqHeader> savedHeaderList = new ArrayList<>();
        for (BcqHeader header: headerList) {
            long headerId = saveHeader(header);
            List<BcqData> dataList = header.getDataList();
            String sql = header.isExists() ? updateData : insertData;
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BcqData data = dataList.get(i);
                    if (header.isExists()) {
                        ps.setString(1, data.getReferenceMtn());
                        ps.setBigDecimal(2, data.getBcq());
                        ps.setTimestamp(3, new Timestamp(data.getEndTime().getTime()));
                        ps.setLong(4, headerId);
                    } else {
                        ps.setLong(1, headerId);
                        ps.setString(2, data.getReferenceMtn());
                        ps.setTimestamp(3, new Timestamp(data.getStartTime().getTime()));
                        ps.setTimestamp(4, new Timestamp(data.getEndTime().getTime()));
                        ps.setBigDecimal(5, data.getBcq());
                    }
                }
                @Override
                public int getBatchSize() {
                    return dataList.size();
                }
            });
            header.setHeaderId(headerId);
            savedHeaderList.add(header);
        }
        return savedHeaderList;
    }

    @Override
    public Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest) {
        int totalRecords = getTotalRecords(pageableRequest);
        Map<String, String> params = pageableRequest.getMapParams();
        String sellingMtn = params.get("sellingMtn");
        String billingId = params.get("billingId");
        String buyingParticipant = params.get("buyingParticipant");
        String sellingParticipant = params.get("sellingParticipant");
        String status = params.get("status");
        Date tradingDate = parseDate(params.get("tradingDate"));
        BcqQueryBuilder builder = new BcqQueryBuilder(displayPaginate);
        BuilderData selectQuery = builder.newQuery(displayData)
                .addTradingDateFilter(tradingDate)
                .addSellingMtnFilter(sellingMtn)
                .addBillingIdFilter(billingId)
                .addBuyingParticipantFilter(buyingParticipant)
                .addSellingParticipantFilter(sellingParticipant)
                .addStatusFilter(status)
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageableRequest.getPageNo(), pageableRequest.getPageSize())
                .build();
        log.debug("[DAO-BCQ] Finding page of headers with query: {}, and args: {}",
                selectQuery.getSql(), selectQuery.getArguments());
        List<BcqHeader> headerList = jdbcTemplate.query(selectQuery.getSql(), selectQuery.getArguments(),
                new BcqHeaderRowMapper());
        log.debug("[DAO-BCQ] Found {} headers", headerList.size());
        return new PageImpl<>(headerList, pageableRequest.getPageable(), totalRecords);
    }

    @Override
    public List<BcqHeader> findAllHeaders(Map<String, String> params) {
        Long headerId = params.get("headerId") == null ? null : parseLong(params.get("headerId"));
        String sellingMtn = params.get("sellingMtn");
        String billingId = params.get("billingId");
        String buyingParticipant = params.get("buyingParticipant");
        String sellingParticipant = params.get("sellingParticipant");
        String status = params.get("status");
        Date tradingDate = params.get("tradingDate") == null ? null : parseDate(params.get("tradingDate"));
        boolean expired = params.get("expired") != null;
        BcqQueryBuilder builder = new BcqQueryBuilder();
        BuilderData selectQuery = builder.newQuery(displayData)
                .addHeaderIdFilter(headerId)
                .addTradingDateFilter(tradingDate)
                .addSellingMtnFilter(sellingMtn)
                .addBillingIdFilter(billingId)
                .addBuyingParticipantFilter(buyingParticipant)
                .addSellingParticipantFilter(sellingParticipant)
                .addStatusFilter(status)
                .addExpiredFilter(expired)
                .build();
        log.debug("[DAO-BCQ] Finding all headers with query: {}, and args: {}",
                selectQuery.getSql(), selectQuery.getArguments());
        List<BcqHeader> headerList = jdbcTemplate.query(selectQuery.getSql(), selectQuery.getArguments(),
                new BcqHeaderRowMapper());
        log.debug("[DAO-BCQ] Found {} headers", headerList.size());
        return headerList;
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        BcqQueryBuilder builder = new BcqQueryBuilder(displayData);
        BuilderData query = builder.newQuery(displayData)
                .addHeaderIdFilter(headerId)
                .build();
        log.debug("[DAO-BCQ] Finding header with ID: {}", headerId);
        try {
            BcqHeader header = jdbcTemplate.queryForObject(query.getSql(), query.getArguments(), new BcqHeaderRowMapper());
            log.debug("[DAO-BCQ] Found header: {}", header);
            return header;
        } catch (EmptyResultDataAccessException ex) {
            log.debug("[DAO-BCQ] No header found with ID: {}", headerId);
            return null;
        }
    }

    @Override
    public List<BcqData> findDataByHeaderId(long headerId) {
        log.debug("[DAO-BCQ] Finding data of header with ID: {}", headerId);
        List<BcqData> dataList = jdbcTemplate.query(dataDetails, new Object[]{headerId},
                rs -> {
                    List<BcqData> content = new ArrayList<>();
                    while (rs.next()) {
                        BcqData bcqData = new BcqData();
                        bcqData.setReferenceMtn(rs.getString("reference_mtn"));
                        bcqData.setEndTime(rs.getTime("end_time"));
                        bcqData.setBcq(rs.getBigDecimal("bcq"));
                        content.add(bcqData);
                    }
                    return content;
                });
        log.debug("[DAO-BCQ] Found {} data of header with ID: {}", dataList.size(), headerId);
        return dataList;
    }

    @Override
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        log.debug("[DAO-BCQ] Updating status of header to {} with ID: {}", status, headerId);
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(updateHeaderStatus);
                    ps.setString(1, status.toString());
                    ps.setLong(2, headerId);
                    return ps;
                });
        log.debug("[DAO-BCQ] Updated status of header with ID: {} to {} ", headerId, status);
    }


    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private long saveHeader(BcqHeader header) {
        log.debug("[DAO-BCQ] Saving header: {}, {}, {}",
                header.getSellingMtn(), header.getBillingId(), header.getTradingDate());
        long tradingDateInMillis = header.getTradingDate().getTime();
        if (header.isExists()) {
            log.debug("[DAO-BCQ] Header exists, doing an update.");
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(updateHeader);
                ps.setLong(1, header.getFileId());
                ps.setString(2, header.getStatus().toString());
                ps.setString(3, header.getUpdatedVia());
                ps.setString(4, header.getSellingMtn());
                ps.setString(5, header.getBillingId());
                ps.setTimestamp(6, new Timestamp(tradingDateInMillis));
                ps.setString(7, header.getSellingParticipantShortName());
                return ps;
            });
            return header.getHeaderId();
        } else {
            long deadlineConfigInSeconds = DAYS.toSeconds(getDeadlineConfig());
            Timestamp deadlineDateTimestamp = new Timestamp(tradingDateInMillis +
                    SECONDS.toMillis(deadlineConfigInSeconds - 1));
            KeyHolder keyHolder = new GeneratedKeyHolder();
            log.debug("[DAO-BCQ] New header, doing an insert.");
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(insertHeader, new String[]{"bcq_header_id"});
                ps.setLong(1, header.getFileId());
                ps.setString(2, header.getSellingMtn());
                ps.setString(3, header.getBillingId());
                ps.setLong(4, header.getBuyingParticipantUserId());
                ps.setString(5, header.getBuyingParticipantName());
                ps.setString(6, header.getBuyingParticipantShortName());
                ps.setLong(7, header.getSellingParticipantUserId());
                ps.setString(8, header.getSellingParticipantName());
                ps.setString(9, header.getSellingParticipantShortName());
                ps.setString(10, header.getStatus().toString());
                ps.setTimestamp(11, new Timestamp(tradingDateInMillis));
                ps.setTimestamp(12, deadlineDateTimestamp);
                return ps;
            }, keyHolder);
            return keyHolder.getKey().longValue();
        }
    }

    private int getDeadlineConfig() {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper deadlineWrapper = configCache.get("BCQ_NULLIFICATION_DEADLINE");
        return deadlineWrapper == null ? 2 :
                parseInt(configCache.get("BCQ_NULLIFICATION_DEADLINE").get().toString()) + 1;
    }

    private int getTotalRecords(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();
        String sellingMtn = params.get("sellingMtn");
        String billingId = params.get("billingId");
        String sellingParticipant = params.get("sellingParticipant");
        String buyingParticipant = params.get("buyingParticipant");
        Date tradingDate = parseDate(params.get("tradingDate"));
        String status = params.get("status");

        BcqQueryBuilder builder = new BcqQueryBuilder();
        BuilderData countQuery = builder.newQuery(displayCount)
                .addTradingDateFilter(tradingDate)
                .addSellingMtnFilter(sellingMtn)
                .addBillingIdFilter(billingId)
                .addBuyingParticipantFilter(buyingParticipant)
                .addSellingParticipantFilter(sellingParticipant)
                .addStatusFilter(status)
                .build();

        return jdbcTemplate.queryForObject(
                countQuery.getSql(),
                countQuery.getArguments(),
                Integer.class);
    }




    private class BcqHeaderRowMapper implements RowMapper<BcqHeader> {

        @Override
        public BcqHeader mapRow(ResultSet rs, int rowNum) throws SQLException {
            BcqHeader header = new BcqHeader();
            BcqUploadFile uploadFile = new BcqUploadFile();
            header.setHeaderId(rs.getLong("bcq_header_id"));
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
            header.setUpdatedVia(rs.getString("updated_via"));
            header.setStatus(fromString(rs.getString("status")));
            uploadFile.setTransactionId(rs.getString("transaction_id"));
            uploadFile.setSubmittedDate(rs.getTimestamp("submitted_date"));
            header.setUploadFile(uploadFile);
            return header;
        }

    }
}

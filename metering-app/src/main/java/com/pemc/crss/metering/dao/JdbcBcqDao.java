package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
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

import static com.pemc.crss.metering.constants.ConfigKeys.BCQ_NULLIFICATION_DEADLINE;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Repository
@Slf4j
public class JdbcBcqDao implements BcqDao {

    @Value("${bcq.manifest}")
    private String insertManifest;

    @Value("${bcq.header.insert}")
    private String insertHeader;

    @Value("${bcq.header.update}")
    private String updateHeader;

    @Value("${bcq.header.count}")
    private String countHeader;

    @Value("${bcq.header.status}")
    private String updateHeaderStatus;

    @Value("${bcq.header.id}")
    private String selectHeaderId;

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

    @Autowired
    public JdbcBcqDao(JdbcTemplate jdbcTemplate, CacheManager cacheManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheManager = cacheManager;
    }

    @Override
    public long saveUploadFile(BcqUploadFile bcqUploadFile) {
        log.debug("[BCQ-DAO] Saving file: {}", bcqUploadFile);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertManifest, new String[]{"file_id"});
                    ps.setString(1, bcqUploadFile.getTransactionId());
                    ps.setString(2, bcqUploadFile.getFileName());
                    ps.setLong(3, bcqUploadFile.getFileSize());
                    ps.setTimestamp(4, new Timestamp(bcqUploadFile.getSubmittedDate().getTime()));
                    ps.setString(5, bcqUploadFile.getValidationStatus().toString());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Long> saveBcq(long fileId, List<BcqHeader> headerList) {
        List<Long> headerIds = new ArrayList<>();

        for (BcqHeader header: headerList) {
            boolean headerExists = headerExists(header);
            long headerId = saveBcqHeader(fileId, header, headerExists);
            List<BcqData> dataList = header.getDataList();

            String sql = headerExists ? updateData : insertData;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BcqData data = dataList.get(i);

                    if (headerExists) {
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

            headerIds.add(headerId);
        }

        return headerIds;
    }

    @Override
    public List<BcqHeader> findAllHeaders(Map<String, String> params) {
        Long headerId = params.get("headerId") == null ? null : Long.parseLong(params.get("headerId"));
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

        log.debug("[BCQ-DAO] Find all headers with query: {}, and args: {}",
                selectQuery.getSql(), selectQuery.getArguments());

        return jdbcTemplate.query(
                selectQuery.getSql(),
                selectQuery.getArguments(),
                new BcqHeaderRowMapper());
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

        List<BcqHeader> headerList = jdbcTemplate.query(
                selectQuery.getSql(),
                selectQuery.getArguments(),
                new BcqHeaderRowMapper());

        log.debug("[BCQ-DAO] Find all headers page with query: {}, and args: {}",
                selectQuery.getSql(), selectQuery.getArguments());

        return new PageImpl<>(
                headerList,
                pageableRequest.getPageable(),
                totalRecords);
    }

    @Override
    public BcqHeader findHeader(long headerId) {
        BcqQueryBuilder builder = new BcqQueryBuilder(displayData);
        BuilderData query = builder.newQuery(displayData)
                .addHeaderIdFilter(headerId)
                .build();

        return jdbcTemplate.queryForObject(
                query.getSql(),
                query.getArguments(),
                new BcqHeaderRowMapper());
    }

    @Override
    public List<BcqData> findAllBcqData(long headerId) {
        log.debug("[BCQ-DAO] Find all data of header with ID: {}", headerId);
        return jdbcTemplate.query(
                dataDetails,
                new Object[]{headerId},
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
    }

    @Override
    public void updateHeaderStatus(long headerId, BcqStatus status) {
        log.debug("[BCQ-DAO] Updating status of header to {} with ID: {}", status, headerId);
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(updateHeaderStatus);
                    ps.setString(1, status.toString());
                    ps.setLong(2, headerId);

                    return ps;
                });
        log.debug("[BCQ-DAO] Successfully updated status of header with ID: {} to {} ", headerId, status);
    }

    @Override
    public boolean headerExists(BcqHeader header) {
        return jdbcTemplate.queryForObject(countHeader,
                new Object[] {
                        header.getSellingMtn(),
                        header.getBillingId(),
                        header.getTradingDate(),
                        header.getSellingParticipantShortName()
                }, Integer.class) > 0;
    }


    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private long saveBcqHeader(long fileId, BcqHeader header, boolean update) {
        if (update) {
            log.debug("Header exists, doing an update.");

            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(updateHeader);
                ps.setLong(1, fileId);
                ps.setString(2, header.getStatus().toString());
                ps.setString(3, header.getUpdatedVia());
                ps.setString(4, header.getSellingMtn());
                ps.setString(5, header.getBillingId());
                ps.setTimestamp(6, new Timestamp(header.getTradingDate().getTime()));
                ps.setString(7, header.getSellingParticipantShortName());

                return ps;
            });

            return getHeaderIdBy(header);
        } else {
            Cache configCache = cacheManager.getCache("config");
            ValueWrapper deadlineWrapper = configCache.get(BCQ_NULLIFICATION_DEADLINE.toString());
            int deadlineConfig = deadlineWrapper == null ? 2 :
                    Integer.parseInt(configCache.get(BCQ_NULLIFICATION_DEADLINE.toString()).get().toString());
            long deadlineConfigInSeconds = DAYS.toSeconds(deadlineConfig);
            Timestamp deadlineDateTimestamp = new Timestamp(header.getTradingDate().getTime() +
                    SECONDS.toMillis(deadlineConfigInSeconds - 1));

            KeyHolder keyHolder = new GeneratedKeyHolder();
            log.debug("New header, doing an insert.");

            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(insertHeader, new String[]{"bcq_header_id"});
                ps.setLong(1, fileId);
                ps.setString(2, header.getSellingMtn());
                ps.setString(3, header.getBillingId());
                ps.setString(4, header.getBuyingParticipantName());
                ps.setString(5, header.getBuyingParticipantShortName());
                ps.setString(6, header.getSellingParticipantName());
                ps.setString(7, header.getSellingParticipantShortName());
                ps.setString(8, header.getStatus().toString());
                ps.setTimestamp(9, new Timestamp(header.getTradingDate().getTime()));
                ps.setTimestamp(10, deadlineDateTimestamp);

                return ps;
            }, keyHolder);

            return keyHolder.getKey().longValue();
        }
    }

    private long getHeaderIdBy(BcqHeader header) {
        return jdbcTemplate.queryForObject(selectHeaderId,
                new Object[] {
                        header.getSellingMtn(),
                        header.getBillingId(),
                        header.getTradingDate(),
                        header.getSellingParticipantShortName()
                }, Long.class);
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
            header.setBuyingParticipantName(rs.getString("buying_participant_name"));
            header.setBuyingParticipantShortName(rs.getString("buying_participant_short_name"));
            header.setSellingParticipantName(rs.getString("selling_participant_name"));
            header.setSellingParticipantShortName(rs.getString("selling_participant_short_name"));
            header.setTradingDate(rs.getDate("trading_date"));
            header.setDeadlineDate(rs.getTimestamp("deadline_date"));
            header.setUpdatedVia(rs.getString("updated_via"));
            header.setStatus(BcqStatus.fromString(rs.getString("status")));
            uploadFile.setTransactionId(rs.getString("transaction_id"));
            uploadFile.setSubmittedDate(rs.getTimestamp("submitted_date"));
            header.setUploadFile(uploadFile);

            return header;
        }
    }
}

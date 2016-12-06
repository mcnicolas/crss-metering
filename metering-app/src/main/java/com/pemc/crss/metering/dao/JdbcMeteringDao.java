package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.VARCHAR;

@Slf4j
@Repository
public class JdbcMeteringDao implements MeteringDao {

    private final DateFormat readingDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    @Value("${mq.manifest.header.insert}")
    private String insertHeaderManifest;

    @Value("${mq.manifest.header.query}")
    private String queryHeaderManifest;

    @Value("${mq.manifest.trailer}")
    private String addTrailerManifest;

    @Value("${mq.manifest.file.insert}")
    private String insertFileManifest;

    @Value("${mq.manifest.file.query}")
    private String queryFileManifest;

    @Value("${mq.meter.daily}")
    private String insertDailyMQ;

    @Value("${mq.meter.monthly}")
    private String insertMonthlyMQ;

    @Value("${mq.manifest.status}")
    private String updateManifestStatus;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public JdbcMeteringDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public long saveHeader(String transactionID, int fileCount, String category, String username) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertHeaderManifest, new String[]{"header_id"});
                    ps.setString(1, transactionID);
                    ps.setInt(2, fileCount);
                    ps.setString(3, category);
                    ps.setString(4, username);
                    ps.setTimestamp(5, new Timestamp(new Date().getTime()));

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void saveTrailer(String transactionID) {
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(addTrailerManifest);
                    ps.setString(1, transactionID);

                    return ps;
                });
    }

    @Override
    public long saveFileManifest(FileManifest fileManifest) {
        // TODO: Use named query
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertFileManifest, new String[]{"file_id"});
                    ps.setLong(1, fileManifest.getHeaderID());
                    ps.setString(2, fileManifest.getTransactionID());
                    ps.setString(3, fileManifest.getFileName());
                    ps.setString(4, fileManifest.getFileType().toString());
                    ps.setLong(5, fileManifest.getFileSize());
                    ps.setString(6, fileManifest.getChecksum());
                    ps.setTimestamp(7, new Timestamp(new Date().getTime()));

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<MeterDataDisplay> findAll(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();

        String category = params.get("category");
        String readingDate = params.get("readingDate");
        String sein = params.get("sein");
        String transactionID = params.get("transactionID");

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        BuilderData query = queryBuilder.selectMeterData(category, readingDate)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .orderBy(pageableRequest.getOrderList())
                .build();

        int pageNo = pageableRequest.getPageNo();
        int pageSize = pageableRequest.getPageSize();
        int startRow = pageNo * pageSize;

        return jdbcTemplate.query(
                query.getSql(),
                query.getArguments(),
                rs -> {
                    List<MeterDataDisplay> meterDataList = new ArrayList<>();

                    // TODO: Hackish code. Consider using DB specific resultset filtering
                    int currentRow = 0;
                    while (rs.next() && currentRow < startRow + pageSize) {
                        if (currentRow >= startRow) {
                            MeterDataDisplay meterData = new MeterDataDisplay();

                            meterData.setMeterDataID(rs.getLong("meter_data_id"));
                            meterData.setSein(rs.getString("sein"));

                            try {
                                meterData.setReadingDateTime(
                                        readingDateFormat.parse(String.valueOf(rs.getLong("reading_datetime")))
                                );
                            } catch (ParseException e) {
                                log.error(e.getMessage(), e);
                            }

                            meterData.setKwd(rs.getString("kwd"));
                            meterData.setKwhd(rs.getString("kwhd"));
                            meterData.setKvarhd(rs.getString("kvarhd"));
                            meterData.setKwr(rs.getString("kwr"));
                            meterData.setKwhr(rs.getString("kwhr"));
                            meterData.setKvarhr(rs.getString("kvarhr"));
                            meterData.setEstimationFlag(rs.getString("estimation_flag"));

                            meterDataList.add(meterData);
                        }

                        currentRow++;
                    }

                    return meterDataList;
                });
    }

    @Override
    public int getTotalRecords(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();

        String category = params.get("category");
        String readingDate = params.get("readingDate");
        String sein = params.get("sein");
        String transactionID = params.get("transactionID");

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        BuilderData query = queryBuilder.countMeterData(category, readingDate)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .build();

        return jdbcTemplate.queryForObject(
                query.getSql(),
                query.getArguments(),
                Integer.class
        );
    }

    @Override
    public void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails) {
        String insertSQL;

        insertSQL = fileManifest.getUploadType() == DAILY ? insertDailyMQ : insertMonthlyMQ;

        jdbcTemplate.batchUpdate(insertSQL, meterDataDetails, 50, (ps, meterData) -> {
            ps.setLong(1, fileManifest.getFileID());
            ps.setString(2, meterData.getSein());
            ps.setInt(3, meterData.getInterval());

            // TODO: Optimize at the parsing level
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            ps.setLong(4, Long.valueOf(dateFormat.format(meterData.getReadingDateTime())));

            if (meterData.getKwd() != null) {
                ps.setDouble(5, meterData.getKwd());
            } else {
                ps.setNull(5, DOUBLE);
            }

            ps.setInt(6, meterData.getKwdChannelStatus());
            ps.setInt(7, meterData.getKwdIntervalStatus());

            if (meterData.getKwhd() != null) {
                ps.setDouble(8, meterData.getKwhd());
            } else {
                ps.setNull(8, DOUBLE);
            }

            ps.setInt(9, meterData.getKwhdChannelStatus());
            ps.setInt(10, meterData.getKwhdIntervalStatus());

            if (meterData.getKvarhd() != null) {
                ps.setDouble(11, meterData.getKvarhd());
            } else {
                ps.setNull(11, DOUBLE);
            }

            ps.setInt(12, meterData.getKvarhdChannelStatus());
            ps.setInt(13, meterData.getKvarhdIntervalStatus());

            if (meterData.getKwr() != null) {
                ps.setDouble(14, meterData.getKwr());
            } else {
                ps.setNull(14, DOUBLE);
            }

            ps.setInt(15, meterData.getKwrChannelStatus());
            ps.setInt(16, meterData.getKwrIntervalStatus());

            if (meterData.getKwhr() != null) {
                ps.setDouble(17, meterData.getKwhr());
            } else {
                ps.setNull(17, DOUBLE);
            }

            ps.setInt(18, meterData.getKwhrChannelStatus());
            ps.setInt(19, meterData.getKwhrIntervalStatus());

            if (meterData.getKvarhr() != null) {
                ps.setDouble(20, meterData.getKvarhr());
            } else {
                ps.setNull(20, DOUBLE);
            }

            ps.setInt(21, meterData.getKvarhrChannelStatus());
            ps.setInt(22, meterData.getKvarhrIntervalStatus());

            if (meterData.getVan() != null) {
                ps.setDouble(23, meterData.getVan());
            } else {
                ps.setNull(23, DOUBLE);
            }

            ps.setInt(24, meterData.getVanChannelStatus());
            ps.setInt(25, meterData.getVanIntervalStatus());

            if (meterData.getVbn() != null) {
                ps.setDouble(26, meterData.getVbn());
            } else {
                ps.setNull(26, DOUBLE);
            }

            ps.setInt(27, meterData.getVbnChannelStatus());
            ps.setInt(28, meterData.getVbnIntervalStatus());

            if (meterData.getVcn() != null) {
                ps.setDouble(29, meterData.getVcn());
            } else {
                ps.setNull(29, DOUBLE);
            }

            ps.setInt(30, meterData.getVcnChannelStatus());
            ps.setInt(31, meterData.getVcnIntervalStatus());

            if (meterData.getIan() != null) {
                ps.setDouble(32, meterData.getIan());
            } else {
                ps.setNull(32, DOUBLE);
            }

            ps.setInt(33, meterData.getIanChannelStatus());
            ps.setInt(34, meterData.getIanIntervalStatus());

            if (meterData.getIbn() != null) {
                ps.setDouble(35, meterData.getIbn());
            } else {
                ps.setNull(35, DOUBLE);
            }

            ps.setInt(36, meterData.getIbnChannelStatus());
            ps.setInt(37, meterData.getIbnIntervalStatus());

            if (meterData.getIcn() != null) {
                ps.setDouble(38, meterData.getIcn());
            } else {
                ps.setNull(38, DOUBLE);
            }

            ps.setInt(39, meterData.getIcnChannelStatus());
            ps.setInt(40, meterData.getIcnIntervalStatus());

            if (meterData.getPf() != null) {
                ps.setDouble(41, meterData.getPf());
            } else {
                ps.setNull(41, DOUBLE);
            }

            ps.setInt(42, meterData.getPfChannelStatus());
            ps.setInt(43, meterData.getPfIntervalStatus());

            ps.setString(44, meterData.getEstimationFlag());
            ps.setInt(45, 1);

            ps.setString(46, fileManifest.getMspShortName());
        });
    }

    @Override
    public void updateManifestStatus(ValidationResult validationResult) {
        BeanPropertySqlParameterSource paramSource = new BeanPropertySqlParameterSource(validationResult);
        paramSource.registerSqlType("status", VARCHAR);

        int affectedRows = namedParameterJdbcTemplate.update(updateManifestStatus, paramSource);
        log.debug("Finished updating manifest file fileID:{} affectedRows:{}", validationResult.getFileID(), affectedRows);
    }

    @Override
    @Transactional(readOnly = true)
    public HeaderManifest getHeaderManifest(String transactionID) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("txnID", transactionID);

        return namedParameterJdbcTemplate.queryForObject(queryHeaderManifest, paramMap, new BeanPropertyRowMapper<>(HeaderManifest.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileManifest> getFileManifest(String transactionID) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("txnID", transactionID);

        return namedParameterJdbcTemplate.query(queryFileManifest, paramMap, new BeanPropertyRowMapper<>(FileManifest.class));
    }

}

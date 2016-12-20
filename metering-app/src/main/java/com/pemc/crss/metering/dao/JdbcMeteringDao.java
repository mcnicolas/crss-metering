package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.UploadType.CORRECTED_DAILY;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static java.sql.Types.VARCHAR;

@Slf4j
@Repository
public class JdbcMeteringDao implements MeteringDao {

    private final DateFormat readingDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    @Value("${mq.manifest.header.insert}")
    private String insertHeaderManifest;

    @Value("${mq.manifest.header.query}")
    private String queryHeaderManifest;

    @Value("${mq.manifest.trailer.update}")
    private String addTrailerManifest;

    @Value("${mq.manifest.file.insert}")
    private String insertFileManifest;

    @Value("${mq.manifest.file.query}")
    private String queryFileManifest;

    @Value("${mq.meter.daily.insert}")
    private String insertDailyMQ;

    @Value("${mq.meter.monthly.insert}")
    private String insertMonthlyMQ;

    @Value("${mq.manifest.status}")
    private String updateManifestStatus;

    @Value("${mq.manifest.upload.status}")
    private String fileProcessingCompleted;

    @Value("${mq.manifest.upload.report}")
    private String uploadReport;

    @Value("${mq.manifest.filter-by.status}")
    private String filterByHeaderAndStatus;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public JdbcMeteringDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        JdbcExceptionTranslator exceptionTranslator = new JdbcExceptionTranslator();

        this.jdbcTemplate = jdbcTemplate;
        this.jdbcTemplate.setExceptionTranslator(exceptionTranslator);

        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        ((JdbcTemplate) this.namedParameterJdbcTemplate.getJdbcOperations()).setExceptionTranslator(exceptionTranslator);
    }

    @Override
    public long saveHeader(HeaderManifest manifest) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(insertHeaderManifest,
                new BeanPropertySqlParameterSource(manifest), keyHolder, new String[]{"header_id"});

        return keyHolder.getKey().longValue();
    }

    @Override
    public String saveTrailer(long headerID) {
        namedParameterJdbcTemplate.update(addTrailerManifest, new MapSqlParameterSource("headerID", headerID));

        HeaderManifest headerManifest = namedParameterJdbcTemplate.queryForObject(queryHeaderManifest,
                new MapSqlParameterSource("headerID", headerID),
                new BeanPropertyRowMapper<>(HeaderManifest.class));

        return headerManifest.getTransactionID();
    }

    @Override
    public long saveFileManifest(FileManifest fileManifest) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(fileManifest);
        source.registerSqlType("fileType", VARCHAR);

        namedParameterJdbcTemplate.update(insertFileManifest,
                source,
                keyHolder, new String[]{"file_id"});

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<MeterDataDisplay> findAll(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();

        String category = params.get("category");
        String readingDate = params.get("readingDate");
        String sein = params.get("sein");
        String transactionID = params.get("transactionID");
        String mspShortName = params.get("shortName");

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        BuilderData query = queryBuilder.selectMeterData(category, readingDate)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .addMSPFilter(mspShortName)
                .orderBy(pageableRequest.getOrderList())
                .build();

        int pageNo = pageableRequest.getPageNo();
        int pageSize = pageableRequest.getPageSize();
        int startRow = pageNo * pageSize;

        log.debug("Select sql: {}", query.getSql());

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
                            meterData.setTransactionID(rs.getString("transaction_id"));
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
        String mspShortName = params.get("shortName");

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        BuilderData query = queryBuilder.countMeterData(category, readingDate)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .addMSPFilter(mspShortName)
                .build();

        log.debug("Total records sql: {}", query.getSql());

        return jdbcTemplate.queryForObject(
                query.getSql(),
                query.getArguments(),
                Integer.class
        );
    }

    @Override
    public void saveMeterData(FileManifest fileManifest, List<MeterDataDetail> meterDataDetails) {
        String insertSQL;

        if (fileManifest.getUploadType() == DAILY || fileManifest.getUploadType() == CORRECTED_DAILY) {
            insertSQL = insertDailyMQ;
        } else {
            insertSQL = insertMonthlyMQ;
        }

        List<BeanPropertySqlParameterSource> sourceList = new ArrayList<>();
        for (MeterDataDetail meterDataDetail : meterDataDetails) {
            BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(meterDataDetail);
            source.registerSqlType("uploadType", VARCHAR);

            sourceList.add(source);
        }

        namedParameterJdbcTemplate.batchUpdate(insertSQL, sourceList.toArray(new BeanPropertySqlParameterSource[]{}));
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
    public HeaderManifest getHeaderManifest(long headerID) {
        return namedParameterJdbcTemplate.queryForObject(queryHeaderManifest,
                new MapSqlParameterSource("headerID", headerID),
                new BeanPropertyRowMapper<>(HeaderManifest.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileManifest> getFileManifest(long headerID) {
        return namedParameterJdbcTemplate.query(queryFileManifest,
                new MapSqlParameterSource("headerID", headerID),
                new BeanPropertyRowMapper<>(FileManifest.class));
    }

    @Override
    public boolean isFileProcessingCompleted(long headerId) {
        return namedParameterJdbcTemplate.queryForObject(fileProcessingCompleted,
                new MapSqlParameterSource("headerID", headerId),
                Boolean.class);
    }

    @Override
    public MeterQuantityReport getManifestReport(long headerId) {
        MeterQuantityReport report = namedParameterJdbcTemplate.queryForObject(uploadReport,
                new MapSqlParameterSource("headerID", headerId),
                new BeanPropertyRowMapper<>(MeterQuantityReport.class));

        return report;
    }

    @Override
    public List<FileManifest> findByHeaderAndStatus(long headerId, String status) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("headerID", headerId)
                .addValue("status", status);
        return namedParameterJdbcTemplate.query(filterByHeaderAndStatus, paramSource,
                new BeanPropertyRowMapper<>(FileManifest.class));
    }
}

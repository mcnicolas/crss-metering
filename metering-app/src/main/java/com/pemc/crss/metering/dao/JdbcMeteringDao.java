package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.ProcessedMqData;
import com.pemc.crss.metering.dto.VersionData;
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
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.UploadType.CORRECTED_DAILY;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static java.sql.Types.VARCHAR;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Repository
public class JdbcMeteringDao implements MeteringDao {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Deprecated // TODO: Use java.time
    private final DateFormat readingDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    @Value("${mq.manifest.header.insert}")
    private String insertHeaderManifest;

    @Value("${mq.manifest.header.query}")
    private String queryHeaderManifest;

    @Value("${mq.manifest.header.count}")
    private String queryHeaderCount;

    @Value("${mq.manifest.trailer.update}")
    private String addTrailerManifest;

    @Value("${mq.manifest.file.insert}")
    private String insertFileManifest;

    @Value("${mq.manifest.file.status}")
    private String queryFileManifestStatus;

    @Value("${mq.manifest.file.query}")
    private String queryFileManifest;

    @Value("${mq.manifest.file.unprocessed}")
    private String queryUnprocessedFileCount;

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

    @Value("${mq.manifest.upload.notif.status}")
    private String updateHeaderForNotification;

    @Value("${mq.manifest.upload.notif.stale}")
    private String queryStaleRecordsForNotif;

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
    public boolean isHeaderValid(long headerID) {
        int count = namedParameterJdbcTemplate.queryForObject(queryHeaderCount,
                new MapSqlParameterSource("headerID", headerID),
                Integer.class);

        return count > 0;
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
        String readingDateFrom = params.get("readingDateFrom");
        String readingDateTo = params.get("readingDateTo");

        Long dateFrom = getStartOfDay(readingDateFrom);
        Long dateTo = getEndOfDay(readingDateFrom, readingDateTo);

        String sein = params.get("sein");
        String transactionID = params.get("transactionID");
        String mspShortName = params.get("shortName");
        String version = params.get("version");

        if (isNotBlank(version)) {
            transactionID = version.trim();
        }

        int pageNo = pageableRequest.getPageNo();
        int pageSize = pageableRequest.getPageSize();

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        BuilderData query = queryBuilder.selectMeterData(category, dateFrom, dateTo, version)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .addMSPFilter(mspShortName)
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageNo, pageSize)
                .build();

        log.debug("Select sql: {}", query.getSql());

        // TODO: Refactored to named jdbc templated
        return jdbcTemplate.query(
                query.getSql(),
                query.getArguments(),
                rs -> {
                    List<MeterDataDisplay> meterDataList = new ArrayList<>();
                    while (rs.next()) {
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

                        meterData.setKwd(getValue(rs.getBigDecimal("kwd")));
                        meterData.setKwhd(getValue(rs.getBigDecimal("kwhd")));
                        meterData.setKvarhd(getValue(rs.getBigDecimal("kvarhd")));
                        meterData.setKwr(getValue(rs.getBigDecimal("kwr")));
                        meterData.setKwhr(getValue(rs.getBigDecimal("kwhr")));
                        meterData.setKvarhr(getValue(rs.getBigDecimal("kvarhr")));
                        meterData.setEstimationFlag(rs.getString("estimation_flag"));

                        meterDataList.add(meterData);
                    }

                    return meterDataList;
                });
    }

    @Override
    public List<VersionData> getVersionedData(Map<String, String> params) {
        String category = params.get("category");
        String readingDateFrom = params.get("readingDateFrom");
        String readingDateTo = params.get("readingDateTo");

        Long dateFrom = getStartOfDay(readingDateFrom);
        Long dateTo = getEndOfDay(readingDateFrom, readingDateTo);

        String sein = params.get("sein");
        String transactionID = params.get("transactionID");
        String mspShortName = params.get("shortName");

        MQVersionQueryBuilder queryBuilder = new MQVersionQueryBuilder();
        BuilderData query = queryBuilder.selectVersionData(category, dateFrom, dateTo)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .addMSPFilter(mspShortName)
                .orderBy()
                .build();

        log.debug("Version query:{}", query.getSql());

        if (query.getArguments().length > 0) {
            return jdbcTemplate.query(
                    query.getSql(),
                    query.getArguments(),
                    new BeanPropertyRowMapper<>(VersionData.class));
        } else {
            return new ArrayList<>();
        }
    }

    private String getValue(BigDecimal reading) {
        String retVal = "";

        if (reading != null) {
            retVal = reading.stripTrailingZeros().toPlainString();
        }

        return retVal;
    }

    @Override
    public int getTotalRecords(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();

        String category = params.get("category");
        String readingDateFrom = params.get("readingDateFrom");
        String readingDateTo = params.get("readingDateTo");

        Long dateFrom = getStartOfDay(readingDateFrom);
        Long dateTo = getEndOfDay(readingDateFrom, readingDateTo);

        String sein = params.get("sein");
        String transactionID = params.get("transactionID");
        String mspShortName = params.get("shortName");
        String version = params.get("version");

        if (isNotBlank(version)) {
            transactionID = version.trim();
        }

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        BuilderData query = queryBuilder.countMeterData(category, dateFrom, dateTo, version)
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

    private Long getStartOfDay(String readingDate) {
        Long retVal = null;

        if (isNotBlank(readingDate)) {
            LocalDateTime dateTime = LocalDate.parse(readingDate, DATE_FORMAT).atStartOfDay();

            retVal = Long.valueOf(dateTime.format(DATETIME_FORMAT));
        }

        return retVal;
    }

    private Long getEndOfDay(String dateFrom, String dateTo) {
        String localDate = dateTo;
        if (isBlank(dateTo)) {
            localDate = dateFrom;
        }

        if (isNotBlank(localDate)) {
            LocalDateTime dateTime = LocalDate.parse(localDate, DATE_FORMAT).atTime(23, 59);
            return Long.valueOf(dateTime.format(DATETIME_FORMAT));
        } else {
            return null;
        }
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
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("fileID", validationResult.getFileID());
        paramSource.addValue("status", validationResult.getStatus().toString());
        paramSource.addValue("errorDetail", validationResult.getErrorDetail());
        paramSource.addValue("processDateTime", new Date());

        int affectedRows = namedParameterJdbcTemplate.update(updateManifestStatus, paramSource);
        log.debug("Finished updating manifest file fileID:{} affectedRows:{}", validationResult.getFileID(), affectedRows);
    }

    @Override
    public HeaderManifest getHeaderManifest(long headerID) {
        return namedParameterJdbcTemplate.queryForObject(queryHeaderManifest,
                new MapSqlParameterSource("headerID", headerID),
                new BeanPropertyRowMapper<>(HeaderManifest.class));
    }

    @Override
    public List<FileManifest> getFileManifest(long headerID) {
        return namedParameterJdbcTemplate.query(queryFileManifest,
                new MapSqlParameterSource("headerID", headerID),
                new BeanPropertyRowMapper<>(FileManifest.class));
    }

    @Override
    public int getUnprocessedFileCount(long headerID) {
        return namedParameterJdbcTemplate.queryForObject(queryUnprocessedFileCount,
                new MapSqlParameterSource("headerID", headerID),
                Integer.class);
    }

    @Override
    public List<FileManifest> getFileManifestStatus(long headerID) {
        return namedParameterJdbcTemplate.query(queryFileManifestStatus,
                new MapSqlParameterSource("headerID", headerID),
                new BeanPropertyRowMapper<>(FileManifest.class));
    }

    @Override
    public boolean isFileProcessingCompleted(long headerID) {
        return namedParameterJdbcTemplate.queryForObject(fileProcessingCompleted,
                new MapSqlParameterSource("headerID", headerID),
                Boolean.class);
    }

    @Override
    public MeterQuantityReport getManifestReport(long headerId) {
        MeterQuantityReport retVal = null;

        List<MeterQuantityReport> list = namedParameterJdbcTemplate.query(uploadReport,
                new MapSqlParameterSource("headerID", headerId),
                new BeanPropertyRowMapper<>(MeterQuantityReport.class));

        if (isNotEmpty(list)) {
            retVal = list.get(0);
        }

        return retVal;
    }

    @Override
    public List<FileManifest> findByHeaderAndStatus(long headerId, String status) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("headerID", headerId)
                .addValue("status", status);
        return namedParameterJdbcTemplate.query(filterByHeaderAndStatus, paramSource,
                new BeanPropertyRowMapper<>(FileManifest.class));
    }

    @Override
    public void updateNotificationFlag(long headerID) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("headerID", headerID)
                .addValue("dateTime", new Date());
        namedParameterJdbcTemplate.update(updateHeaderForNotification, parameterSource);
    }

    @Override
    public List<Long> getStaleRecords() {
        return namedParameterJdbcTemplate.query(queryStaleRecordsForNotif,
                new SingleColumnRowMapper<>(Long.class));
    }

    @Override
    public List<ProcessedMqData> findAllForExtraction(String category, String sein, String tpShortName,
                                                      String dateFromStr, String dateToStr, boolean isLatest) {
        Long dateFrom = getStartOfDay(dateFromStr);
        Long dateTo = getEndOfDay(dateFromStr, dateToStr);

        log.debug("dateFrom={}, dateTo={}", dateFrom, dateTo);
        MQExportQueryBuilder queryBuilder = new MQExportQueryBuilder();
        BuilderData query = queryBuilder.selectMeterData(category, dateFrom, dateTo, isLatest)
                .addSEINFilter(sein)
                .addTpShortnameFilter(tpShortName)
//                .orderBy(pageableRequest.getOrderList())
//                .paginate(pageNo, pageSize)
                .build();


        return getMeterDataDisplay(query);
    }

    private List<ProcessedMqData> getMeterDataDisplay(BuilderData query) {

        log.debug("Select sql: {}", query.getSql());

        return jdbcTemplate.query(
                query.getSql(),
                query.getArguments(),
                rs -> {
                    List<ProcessedMqData> meterDataList = new ArrayList<>();
                    while (rs.next()) {
                        ProcessedMqData meterData = new ProcessedMqData();

                        meterData.setCategory(rs.getString("category"));
                        meterData.setMspShortname(rs.getString("msp_shortname"));
                        meterData.setSein(rs.getString("sein"));

                        try {
                            meterData.setReadingDateTime(
                                    DATE_FORMATTER.format(
                                            readingDateFormat.parse(String.valueOf(rs.getLong("reading_datetime"))))
                            );
                        } catch (ParseException e) {
                            log.error(e.getMessage(), e);
                        }

                        meterData.setKwhd(getBigDecimalValue(rs.getBigDecimal("kwhd")));
                        meterData.setKvarhd(getBigDecimalValue(rs.getBigDecimal("kvarhd")));
                        meterData.setKwd(getBigDecimalValue(rs.getBigDecimal("kwd")));
                        meterData.setKwhr(getBigDecimalValue(rs.getBigDecimal("kwhr")));
                        meterData.setKvarhr(getBigDecimalValue(rs.getBigDecimal("kvarhr")));
                        meterData.setKwr(getBigDecimalValue(rs.getBigDecimal("kwr")));
                        meterData.setEstimationFlag(rs.getString("estimation_flag"));

                        try {
                            meterData.setUploadDateTime(
                                    DATE_FORMATTER.format(rs.getDate("upload_datetime"))
                            );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        meterData.setTransactionId(rs.getString("transaction_id"));

                        meterDataList.add(meterData);

                    }

                    return meterDataList;
                });
    }

    private Object getBigDecimalValue(BigDecimal big) {
        return big == null ? "" : big;
    }
}

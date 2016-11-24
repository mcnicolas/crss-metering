package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.sql.Types.DOUBLE;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Repository
public class JdbcMeteringDao implements MeteringDao {

    @Value("${mq.manifest.header}")
    private String insertHeaderManifest;

    @Value("${mq.manifest.trailer}")
    private String addTrailerManifest;

    @Value("${mq.manifest.file}")
    private String insertFileManifest;

    @Value("${mq.meter.daily}")
    private String insertDailyMQ;

    @Value("${mq.meter.monthly}")
    private String insertMonthlyMQ;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcMeteringDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    public long saveFileManifest(long headerID, String transactionID, String fileName, String fileType, long fileSize,
                                 String checksum) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertFileManifest, new String[]{"file_id"});
                    ps.setLong(1, headerID);
                    ps.setString(2, transactionID);
                    ps.setString(3, fileName);
                    ps.setString(4, fileType);
                    ps.setLong(5, fileSize);
                    ps.setString(6, checksum);

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
        MQBuilderData query = queryBuilder.selectMeterData(category, readingDate)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .orderBy(pageableRequest.getOrderList())
                .build();

        int pageNo = pageableRequest.getPageNo();
        int pageSize = pageableRequest.getPageSize();
        int startRow = pageNo * pageSize;

        List<MeterDataDisplay> retVal = jdbcTemplate.query(
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
                            meterData.setReadingDateTime(rs.getLong("reading_datetime"));
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

        return retVal;
    }

    @Override
    public int getTotalRecords(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();

        String category = params.get("category");
        String readingDate = params.get("readingDate");
        String sein = params.get("sein");
        String transactionID = params.get("transactionID");

        MQDisplayQueryBuilder queryBuilder = new MQDisplayQueryBuilder();
        MQBuilderData query = queryBuilder.countMeterData(category, readingDate)
                .addSEINFilter(sein)
                .addTransactionIDFilter(transactionID)
                .build();

        return jdbcTemplate.queryForObject(
                query.getSql(),
                query.getArguments(),
                Integer.class
        );
    }

    // TODO: Dirty code. Revise!
    @Override
    public void saveMeterData(long fileID, List<MeterData2> meterDataList, String mspShortName, String category) {
        String insertSQL;
        if (equalsIgnoreCase(category, "Monthly")) {
            insertSQL = insertMonthlyMQ;
        } else {
            insertSQL = insertDailyMQ;
        }

        // TODO: Use batch update
        for (MeterData2 meterData : meterDataList) {
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(insertSQL);

                        ps.setLong(1, fileID);
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

                        ps.setString(46, mspShortName);
                        return ps;
                    });
        }
    }

    @Deprecated
    @Override
    public long saveMeterUploadHeader(MeterUploadHeader meterUploadHeader) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_METER_UPLOAD_HEADER (transaction_id, msp_id, category, upload_by," +
                " upload_datetime, version)" +
                " VALUES (NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"transaction_id"});
                    ps.setLong(1, meterUploadHeader.getMspID());
                    ps.setString(2, meterUploadHeader.getCategory());
                    ps.setString(3, meterUploadHeader.getUploadedBy());
                    ps.setTimestamp(4, new Timestamp(meterUploadHeader.getUploadedDateTime().getTime()));
                    ps.setInt(5, meterUploadHeader.getVersion());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Deprecated
    @Override
    public long saveMeterUploadFile(long transactionID, MeterUploadFile meterUploadFile) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_METER_UPLOAD_FILE (FILE_ID, TRANSACTION_ID, FILENAME, FILETYPE, FILESIZE, STATUS)" +
                " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"file_id"});
                    ps.setLong(1, transactionID);
                    ps.setString(2, meterUploadFile.getFileName());
                    ps.setString(3, meterUploadFile.getFileType().toString());
                    ps.setLong(4, meterUploadFile.getFileSize());
                    ps.setString(5, meterUploadFile.getStatus().toString());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Deprecated
    @Override
    public void saveMeterUploadMDEF(long fileID, MeterData meterData) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_METER_HEADER (METER_HEADER_ID, FILE_ID, CUSTOMER_ID, CUSTOMER_NAME,"
                + " CUSTOMER_ADDR1, CUSTOMER_ADDR2, ACCOUNT_NO, TOTAL_CHANNELS, START_DATETIME, END_DATETIME, DST_FLAG)"
                + " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    Header header = meterData.getHeader();

                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"meter_header_id"});
                    ps.setLong(1, fileID);
                    ps.setString(2, header.getCustomerID());
                    ps.setString(3, header.getCustomerName());
                    ps.setString(4, header.getCustomerAddress1());
                    ps.setString(5, header.getCustomerAddress2());
                    ps.setString(6, header.getCustomerAccountNo());
                    ps.setLong(7, 1L);
                    ps.setTimestamp(8, new Timestamp(new Date().getTime()));
                    ps.setTimestamp(9, new Timestamp(new Date().getTime()));
                    ps.setString(10, header.getDstFlag());

                    return ps;
                },
                keyHolder);

        long meterHeaderID = keyHolder.getKey().longValue();

        List<ChannelHeader> channelHeaderList = meterData.getChannels();

        // TODO: Transfer SQL scripts to resource file
        String INSERT_CHANNEL_SQL = "INSERT INTO TXN_CHANNEL_HEADER (CHANNEL_HEADER_ID, METER_HEADER_ID, RECORD_ID,"
                + " METER_NO, START_DATETIME, STOP_DATETIME, METER_CHANNEL_NO, CUSTOMER_CHANNEL_NO, UOM_CODE,"
                + " CHANNEL_STATUS, INTERVAL_STATUS, START_METER, STOP_METER, METER_MULTIPLIER, SERVER_TYPE,"
                + " INTERVAL_PER_HOUR, VALIDATION_RESULT, FLOW_DIRECTION, KVA_SET, ORIGIN)"
                + " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (ChannelHeader channelHeader : channelHeaderList) {
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_CHANNEL_SQL, new String[]{"channel_header_id"});
                        ps.setLong(1, meterHeaderID);
                        ps.setString(2, channelHeader.getRecorderID());
                        ps.setString(3, channelHeader.getMeterNo());
                        ps.setString(4, channelHeader.getStartTime());
                        ps.setString(5, channelHeader.getStopTime());
                        ps.setString(6, channelHeader.getMeterChannelNo());
                        ps.setInt(7, channelHeader.getCustomerChannelNo());
                        ps.setString(8, channelHeader.getUomCode());
                        ps.setBoolean(9, channelHeader.isChannelStatusPresent());
                        ps.setBoolean(10, channelHeader.isIntervalStatusPresent());
                        ps.setString(11, channelHeader.getStartMeterReading());
                        ps.setString(12, channelHeader.getStopMeterReading());
                        ps.setString(13, channelHeader.getMeterMultiplier());
                        ps.setString(14, channelHeader.getServerType());
                        ps.setInt(15, channelHeader.getIntervalPerHour());
                        ps.setString(16, channelHeader.getValidationResults());
                        ps.setString(17, channelHeader.getPowerFlowDirection());
                        ps.setInt(18, channelHeader.getKvaSet());
                        ps.setString(19, channelHeader.getDataOrigin());

                        return ps;
                    },
                    keyHolder);

            long channelHeaderID = keyHolder.getKey().longValue();

            List<IntervalData> intervalList = channelHeader.getIntervals();

            // TODO: Transfer SQL scripts to resource file
            String INSERT_INTERVAL_SQL = "INSERT INTO TXN_INTERVAL (INTERVAL_ID, CHANNEL_HEADER_ID, METER_READING,"
                    + " CHANNEL_STATUS, INTERVAL_STATUS, READING_DATETIME)"
                    + " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?)";

            for (IntervalData intervalData : intervalList) {
                List<Float> meterReading = intervalData.getMeterReading();
                List<Integer> channelStatus = intervalData.getChannelStatus();
                List<Integer> intervalStatus = intervalData.getIntervalStatus();
                List<String> readingDate = intervalData.getReadingDate();

                for (int i = 0; i < intervalData.getMeterReading().size(); i++) {
                    int index = i;
                    jdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection.prepareStatement(INSERT_INTERVAL_SQL);
                                ps.setLong(1, channelHeaderID);
                                ps.setDouble(2, meterReading.get(index));
                                ps.setInt(3, channelStatus.get(index));
                                ps.setInt(4, intervalStatus.get(index));
                                ps.setString(5, readingDate.get(index));

                                return ps;
                            });
                }
            }
        }
    }

}

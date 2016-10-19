package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.ChannelHeader;
import com.pemc.crss.metering.dto.Header;
import com.pemc.crss.metering.dto.IntervalData;
import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterDataXLS;
import com.pemc.crss.metering.dto.MeterUploadFile;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
public class JdbcMeteringDao implements MeteringDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public long saveHeader(String transactionID, long mspID, int fileCount, String category, String username) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_MANIFEST_HEADER (header_id, transaction_id, msp_id, file_count, category," +
                " upload_by, upload_datetime)" +
                " VALUES (NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"header_id"});
                    ps.setString(1, transactionID);
                    ps.setLong(2, mspID);
                    ps.setInt(3, fileCount);
                    ps.setString(4, category);
                    ps.setString(5, username);
                    ps.setTimestamp(6, new Timestamp(new Date().getTime()));

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void saveTrailer(String transactionID) {
        // TODO: Transfer SQL scripts to resource file
        String UPDATE_SQL = "UPDATE TXN_MANIFEST_HEADER SET tail_received = 'Y' WHERE transaction_id = ?";

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
                    ps.setString(1, transactionID);

                    return ps;
                });
    }

    @Override
    public void saveFileManifest(long headerID, String transactionID, String fileName, String fileType, long fileSize,
                                 String checksum) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_MANIFEST_FILE (file_id, header_id, transaction_id, filename, filetype," +
                " filesize, checksum)" +
                " VALUES (NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                    ps.setLong(1, headerID);
                    ps.setString(2, transactionID);
                    ps.setString(3, fileName);
                    ps.setString(4, fileType);
                    ps.setLong(5, fileSize);
                    ps.setString(6, checksum);

                    return ps;
                });
    }

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
                        ps.setString(9, channelHeader.getChannelStatusPresent());
                        ps.setString(10, channelHeader.getIntervalStatusPresent());
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
                List<String> channelStatus = intervalData.getChannelStatus();
                List<String> intervalStatus = intervalData.getIntervalStatus();
                List<String> readingDate = intervalData.getReadingDate();

                for (int i = 0; i < intervalData.getMeterReading().size(); i++) {
                    int index = i;
                    jdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection.prepareStatement(INSERT_INTERVAL_SQL);
                                ps.setLong(1, channelHeaderID);
                                ps.setDouble(2, meterReading.get(index));
                                ps.setString(3, channelStatus.get(index));
                                ps.setString(4, intervalStatus.get(index));
                                ps.setString(5, readingDate.get(index));

                                return ps;
                            });
                }
            }
        }
    }

    @Override
    public void saveMeterUploadXLS(long transactionID, List<MeterDataXLS> meterDataList) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_METER_DATA_XLS (METER_DATA_ID, FILE_ID, SEIN, READING_DATETIME,"
                + " KWD, KWHD, KVARHD, KWR, KWHR, KVARHR)"
                + " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // TODO: Use batch update
        for (MeterDataXLS meterDataXLS : meterDataList) {
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                        ps.setLong(1, transactionID);
                        ps.setString(2, meterDataXLS.getSein());
                        ps.setTimestamp(3, new Timestamp(meterDataXLS.getReadingDateTime().getTime()));
                        ps.setDouble(4, meterDataXLS.getKwd());
                        ps.setDouble(5, meterDataXLS.getKwhd());
                        ps.setDouble(6, meterDataXLS.getKvarhd());
                        ps.setDouble(7, meterDataXLS.getKwr());
                        ps.setDouble(8, meterDataXLS.getKwhr());
                        ps.setDouble(9, meterDataXLS.getKvarhr());

                        return ps;
                    });
        }
    }

}

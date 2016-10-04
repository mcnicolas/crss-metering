package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.ChannelHeader;
import com.pemc.crss.metering.dto.Header;
import com.pemc.crss.metering.dto.IntervalData;
import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.dto.MeterUploadMDEF;
import com.pemc.crss.metering.dto.MeterUploadXLS;
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

                    // TODO: Change to propert date/time
                    ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                    ps.setInt(5, meterUploadHeader.getVersion());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public long saveMeterUploadFile(long transactionID, MeterUploadMDEF meterUploadMDEF) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_METER_UPLOAD_FILE (FILE_ID, TRANSACTION_ID, FILENAME, FILETYPE, FILESIZE, CHECKSUM, STATUS)" +
                " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"file_id"});
                    ps.setLong(1, transactionID);
                    ps.setString(2, meterUploadMDEF.getFileName());
                    ps.setString(3, meterUploadMDEF.getFileType());
                    ps.setLong(4, meterUploadMDEF.getFileSize());
                    ps.setString(5, meterUploadMDEF.getChecksum());
                    ps.setString(6, meterUploadMDEF.getStatus());

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
    public void saveMeterUploadXLS(long transactionID, MeterUploadXLS meterUploadXLS) {
        // TODO: Transfer SQL scripts to resource file
        String INSERT_SQL = "INSERT INTO TXN_METER_DATA_XLS (METER_DATA_ID, FILE_ID, CUSTOMER_ID, READING_DATETIME,"
                + " METER_NO, CHANNEL_STATUS,"
                + " CHANNEL_STATUS_DESC, INTERVAL_STATUS, INTERVAL_STATUS_DESC)"
                + " VALUES(NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                    ps.setLong(1, meterUploadXLS.getFileID());
                    ps.setString(2, meterUploadXLS.getCustomerID());
                    ps.setTimestamp(3, new Timestamp(new Date().getTime()));
                    ps.setString(4, meterUploadXLS.getMeterNo());
                    ps.setString(5, meterUploadXLS.getChannelStatus());
                    ps.setString(6, meterUploadXLS.getChannelStatusDesc());
                    ps.setString(7, meterUploadXLS.getIntervalStatus());
                    ps.setString(8, meterUploadXLS.getIntervalStatusDesc());

                    return ps;
                });
    }

}

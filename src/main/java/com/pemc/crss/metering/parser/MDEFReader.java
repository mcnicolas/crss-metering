package com.pemc.crss.metering.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.pemc.crss.metering.parser.ParserUtil.convertToBinaryString;
import static com.pemc.crss.metering.parser.ParserUtil.parseText;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Calendar.MINUTE;

class MDEFReader {

    private static final Logger LOG = LoggerFactory.getLogger(MDEFReader.class);

    private static final int RECORD_BLOCK_SIZE = 216;

    private String intervalStartDateForChannel = "";

    private int minuteInterval = 15;
    private static final int MINUTES_IN_HOUR = 60;

    MeterData readMDEF(FileInputStream inputStream) throws Exception {

        MeterData meterData = new MeterData();

        try (InputStream data = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[RECORD_BLOCK_SIZE];

            String channelHeaderTaStop = "";

            ChannelHeader channelHeader = null;

            while (data.read(buffer, 0, RECORD_BLOCK_SIZE) >= 0) {

                RecordCode recordCode = RecordCode.getRecordCode(buffer);

                // TODO: Change to strategy pattern
                switch (recordCode) {
                    case METER_HEADER:
                        channelHeader = null;

                        meterData.setHeader(readMeterHeader(buffer));

                        break;
                    case CHANNEL_HEADER:
                        channelHeader = readChannelHeader(buffer);
                        meterData.addChannel(channelHeader);

                        intervalStartDateForChannel = channelHeader.getStartTime();
                        channelHeaderTaStop = channelHeader.getStopTime();
                        int channelIntervalPerHour = Integer.valueOf(channelHeader.getIntervalPerHour());

                        minuteInterval = MINUTES_IN_HOUR / channelIntervalPerHour;

                        break;
                    case INTERVAL_DATA:
                        IntervalData intervalData = readIntervalData(buffer, intervalStartDateForChannel, channelHeaderTaStop);

                        if (intervalData != null && channelHeader != null) {
                            channelHeader.addInterval(intervalData);
                        }

                        break;
                    case TRAILER_RECORD:
                    default:
                        meterData.setTrailerRecord(readTrailer(buffer));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw e;
        }

        return meterData;
    }

    private Header readMeterHeader(byte[] record) {
        Header header = new Header();

        ByteBuffer buffer = ByteBuffer.wrap(record, 0, RECORD_BLOCK_SIZE);
        buffer.order(LITTLE_ENDIAN);

        header.setRecordLength(buffer.getShort());
        header.setRecordCode(buffer.getShort());
        header.setCustomerID(parseText(buffer, 20));
        header.setCustomerName(parseText(buffer, 20));
        header.setCustomerAddress1(parseText(buffer, 20));
        header.setCustomerAddress2(parseText(buffer, 20));
        header.setCustomerAccountNo(parseText(buffer, 20));
        header.setTotalChannels(parseText(buffer, 7, 4));
        header.setStartTime(parseText(buffer, 4, 12));
        header.setStopTime(parseText(buffer, 12));
        header.setDstFlag(parseText(buffer, 1));

        return header;
    }

    private ChannelHeader readChannelHeader(byte[] record) {
        ChannelHeader retVal = new ChannelHeader();

        ByteBuffer buffer = ByteBuffer.wrap(record, 0, RECORD_BLOCK_SIZE);
        buffer.order(LITTLE_ENDIAN);

        retVal.setRecordLength(buffer.getShort());
        retVal.setRecordCode(buffer.getShort());
        retVal.setCustomerID(parseText(buffer, 20));
        retVal.setRecorderID(parseText(buffer, 14));
        retVal.setMeterNo(parseText(buffer, 6, 12));
        retVal.setStartTime(parseText(buffer, 12));
        retVal.setStopTime(parseText(buffer, 12));

        retVal.setMeterChannelNo(parseText(buffer, 13, 2));
        retVal.setCustomerChannelNo(buffer.getShort());
        retVal.setUomCode(parseText(buffer, 2));

        retVal.setChannelStatusPresent(parseText(buffer, 1));
        retVal.setIntervalStatusPresent(parseText(buffer, 1));

        retVal.setStartMeterReading(parseText(buffer, 12));
        retVal.setStopMeterReading(parseText(buffer, 12));
        retVal.setMeterMultiplier(parseText(buffer, 1, 10));
        retVal.setServerType(parseText(buffer, 30, 1));

        retVal.setIntervalPerHour(parseText(buffer, 10, 2));
        retVal.setValidationResults(parseText(buffer, 14, 2));
        retVal.setPowerFlowDirection(parseText(buffer, 15, 1));
        retVal.setKvaSet(buffer.getShort());
        retVal.setDataOrigin(parseText(buffer, 1));

        return retVal;
    }

    // TODO: Use of intervalStartDateForChannel has a side-effect. Need to refactor
    private IntervalData readIntervalData(byte[] buffer, String intervalStartDate, String channelHeaderTaStop) throws Exception {
        IntervalData retVal = new IntervalData();

        List<Float> meterReadings = new ArrayList<>();
        List<String> channelStatusList = new ArrayList<>();
        List<String> intervalStatusList = new ArrayList<>();
        List<String> readingDates = new ArrayList<>();

        DateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

        String readingDate = "";
        String channelStatus;
        String intervalStatus;

        // TODO: Need to inspect this
        format.parse(intervalStartDate);
        Calendar cal = format.getCalendar();

        //round-up to the next quarter
        int unroundedMinutes = cal.get(MINUTE);
        int mod = unroundedMinutes % minuteInterval;

        if (mod != 0) {
            cal.add(MINUTE, (minuteInterval - mod));
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, RECORD_BLOCK_SIZE);
        byteBuffer.order(LITTLE_ENDIAN);

        retVal.setRecordLength(byteBuffer.getShort());
        retVal.setRecordCode(byteBuffer.getShort());

        retVal.setCustomerID(parseText(byteBuffer, 20));

        while (byteBuffer.hasRemaining()) {
            //TODO: should compare by date not String?
            //ignore reading after tastop time. Reading after taStop most probably with trouble
            if (readingDate.equals(channelHeaderTaStop)) {
                break;
            }

            meterReadings.add(byteBuffer.getFloat());

            channelStatus = convertToBinaryString(byteBuffer.getChar());
            intervalStatus = convertToBinaryString(byteBuffer.getChar());

            channelStatusList.add(channelStatus);
            intervalStatusList.add(intervalStatus);

            readingDate = format.format(cal.getTime());
            readingDates.add(readingDate);
            cal.add(MINUTE, minuteInterval);
        }

        retVal.setChannelStatus(channelStatusList);
        retVal.setIntervalStatus(intervalStatusList);
        retVal.setMeterReading(meterReadings);
        retVal.setReadingDate(readingDates);

        //next reading start date for next retVal
        if (readingDates.size() != 0) {
            format.parse(readingDates.get(readingDates.size() - 1));
            cal = format.getCalendar();
            cal.add(MINUTE, minuteInterval);
            intervalStartDateForChannel = format.format(cal.getTime());
        }

        return retVal;
    }

    private TrailerRecord readTrailer(byte[] record) {
        TrailerRecord trailerRecord = new TrailerRecord();

        ByteBuffer buffer = ByteBuffer.wrap(record, 0, RECORD_BLOCK_SIZE);
        buffer.order(LITTLE_ENDIAN);

        trailerRecord.setRecordLength(buffer.getShort());
        trailerRecord.setRecordCode(buffer.getShort());
        trailerRecord.setTotalRecordCount(parseText(buffer, 30, 10));
        trailerRecord.setTimestamp(parseText(buffer, 160, 12));

        return trailerRecord;
    }

}

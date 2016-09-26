package com.pemc.crss.metering.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.parser.MDEFConstants.RECORD_BLOCK_SIZE;
import static com.pemc.crss.metering.parser.ParserUtil.convertToBinaryString;
import static com.pemc.crss.metering.parser.ParserUtil.parseInt;
import static com.pemc.crss.metering.parser.ParserUtil.parseText;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MINUTE;
import static java.util.Locale.ENGLISH;

class MDEFReader {

    private static final Logger LOG = LoggerFactory.getLogger(MDEFReader.class);

    private static final String STATUS_SEPARATOR = "; ";

    private String intervalStartDateForChannel = "";
    private String channelHeaderTaStop = "";

    private int minuteInterval = 15;
    private static final int MINUTES_IN_HOUR = 60;

    MeterData readMDEF(File file) throws Exception {

        MeterData meterData = new MeterData();

        try (InputStream data = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[RECORD_BLOCK_SIZE];

            String recordIntervalStat = "N";
            String recordChannelStat = "N";
//            String dcMeterid = "";

            ChannelHeader channelHeader = null;
            IntervalData intervalData = null;

            while (data.read(buffer, 0, RECORD_BLOCK_SIZE) >= 0) {

                RecordCode code = RecordCode.getRecordCode(buffer);

                // TODO: Change to strategy pattern
                switch (code) {
                    case METER_HEADER:
                        channelHeader = null;
                        intervalData = null;

                        meterData.setHeader(readMeterHeader(buffer));

                        break;
                    case CHANNEL_HEADER:
                        channelHeader = readChannelHeader(buffer);
                        meterData.addChannel(channelHeader);

                        // Note down channel and interval status record availability which is going
                        // to be useful in following interval data record
                        recordIntervalStat = channelHeader.getIntervalStatusPresent();
                        recordChannelStat = channelHeader.getChannelStatusPresent();
                        intervalStartDateForChannel = channelHeader.getStartTime();
                        channelHeaderTaStop = channelHeader.getStopTime();
                        int channelIntervalPerHour = Integer.valueOf(channelHeader.getIntervalPerHour());

                        minuteInterval = MINUTES_IN_HOUR / channelIntervalPerHour;
//                        dcMeterid = channelHeader.getMeterNo();

                        // NOTE: Not sure if we still need this block of code
                        if (intervalData != null && intervalData.getReadingDate().size() != 0) {
                            if (!intervalData.getReadingDate().get(intervalData.getReadingDate().size() - 1).equals(channelHeaderTaStop)) {
                                if (!compareHourlyAndNormalDates(intervalData.getReadingDate().get(intervalData.getReadingDate().size() - 1), channelHeaderTaStop)) {
                                    // allow not equal channel header stop vs. generated interval stop time (there are cases
                                    // like this, i.e, stop time for channel does not match # of interval records where the
                                    // generated interval time depends)
                                    LOG.warn(file.getName() + " WARNING last interval reading date: " + intervalData.getReadingDate().get(intervalData.getReadingDate().size() - 1) + " not equal to channelHeaderTaStop: " + channelHeaderTaStop);
                                }
                            }
                        }

                        break;
                    case INTERVAL_DATA:
                        intervalData = readIntervalData(buffer, recordChannelStat, recordIntervalStat, intervalStartDateForChannel);

                        if (intervalData != null && channelHeader != null) {
                            channelHeader.getIntervals().add(intervalData);
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

        header.setRecordLength(parseInt(0, 1, record));
        header.setRecordCode(parseInt(2, 3, record));
        header.setCustomerID(parseText(4, 23, record));
        header.setCustomerName(parseText(24, 43, record));
        header.setCustomerAddress1(parseText(44, 63, record));
        header.setCustomerAddress2(parseText(64, 83, record));
        header.setCustomerAccountNo(parseText(84, 103, record));
        header.setTotalChannels(parseText(111, 114, record));
        header.setStartTime(parseText(119, 130, record));
        header.setStopTime(parseText(131, 142, record));
        header.setDstFlag(parseText(143, 143, record));

        return header;
    }

    private ChannelHeader readChannelHeader(byte[] record) {
        ChannelHeader channelHeader = new ChannelHeader();

        channelHeader.setRecordLength(parseInt(0, 1, record));
        channelHeader.setRecordCode(parseInt(2, 3, record));
        channelHeader.setCustomerID(parseText(4, 23, record));
        channelHeader.setRecorderID(parseText(24, 37, record));
        channelHeader.setMeterNo(parseText(44, 55, record));
        channelHeader.setStartTime(parseText(56, 67, record));
        channelHeader.setStopTime(parseText(68, 79, record));
        channelHeader.setMeterChannelNo(parseText(93, 94, record));
        channelHeader.setCustomerChannelNo(parseInt(95, 96, record));
        channelHeader.setUomCode(parseText(97, 98, record));
        channelHeader.setChannelStatusPresent(parseText(99, 99, record));
        channelHeader.setIntervalStatusPresent(parseText(100, 100, record));
        channelHeader.setStartMeterReading(parseText(101, 112, record));
        channelHeader.setStopMeterReading(parseText(113, 124, record));
        channelHeader.setMeterMultiplier(parseText(126, 135, record));
        channelHeader.setServerType(parseText(166, 166, record));
        channelHeader.setIntervalPerHour(parseText(177, 178, record));
        channelHeader.setValidationResults(parseText(193, 194, record));
        channelHeader.setPowerFlowDirection(parseText(210, 210, record));
        channelHeader.setKvaSet(parseInt(211, 212, record));
        channelHeader.setDataOrigin(parseText(213, 213, record));

        return channelHeader;
    }

    private IntervalData readIntervalData(byte[] record, String recordChannelStat, String recordIntervalStat,
                                          String intervalStartDate) throws Exception {

        float reading = 0;
        IntervalData interval = new IntervalData();

        List<String> readings = new ArrayList<>();
        List<String> channelStatuses = new ArrayList<>();
        List<String> channelStatusesDesc = new ArrayList<>();

        List<String> intervalStatuses = new ArrayList<>();
        List<String> intervalStatusesDesc = new ArrayList<>();
        List<String> readingDates = new ArrayList<>();

        DateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

        String readingDate = "";
        String channelStatus;
        String intervalStatus;

        format.parse(intervalStartDate);
        Calendar cal = format.getCalendar();

        //round-up to the next quarter
        int unroundedMinutes = cal.get(MINUTE);
        int mod = unroundedMinutes % minuteInterval;
        //int newMinutes = unroundedMinutes;

        if (mod != 0) {
            cal.add(MINUTE, (minuteInterval - mod));
        }

        ByteBuffer buffer = ByteBuffer.wrap(record, 0, RECORD_BLOCK_SIZE);
        buffer.order(LITTLE_ENDIAN);

        interval.setRecordLength(Integer.toString((int) buffer.getChar()));
        interval.setRecordCode(Integer.toString((int) buffer.getChar()));
        interval.setCustomerID(parseText(4, 23, record));

        buffer.position(24);

        if (recordChannelStat.equals("N") && recordIntervalStat.equals("N")) {
            // channel and interval status absent
            while (buffer.hasRemaining()) {
                //TODO: should compare by date not String?
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buffer.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                channelStatus = "0000000000000000";
                channelStatuses.add(channelStatus);
                channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));

                intervalStatus = "0000000000000000";
                intervalStatuses.add(intervalStatus);
                intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));

                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(MINUTE, minuteInterval);

            }

        } else if (recordChannelStat.equals("Y") && recordIntervalStat.equals("N")) {
            // channel status present and interval status absent
            while (buffer.hasRemaining()) {
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buffer.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                if (buffer.hasRemaining()) {
                    channelStatus = convertToBinaryString(buffer.getChar());
                    channelStatuses.add(channelStatus);
                    channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));
                }

                intervalStatus = "0000000000000000";
                intervalStatuses.add(intervalStatus);
                intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));

                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(MINUTE, minuteInterval);
            }

        } else if (recordChannelStat.equals("N") && recordIntervalStat.equals("Y")) {
            // channel status absent and interval status present
            while (buffer.hasRemaining()) {
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buffer.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                channelStatus = "0000000000000000";
                channelStatuses.add(channelStatus);
                channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));

                if (buffer.hasRemaining()) {
                    intervalStatus = convertToBinaryString(buffer.getChar());
                    intervalStatuses.add(intervalStatus);
                    intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));
                }

                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(MINUTE, minuteInterval);
            }

        } else {
            // channel status present and interval status present
            while (buffer.hasRemaining()) {
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buffer.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                if (buffer.hasRemaining()) {
                    channelStatus = convertToBinaryString(buffer.getChar());
                    channelStatuses.add(channelStatus);
                    channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));
                }

                if (buffer.hasRemaining()) {
                    intervalStatus = convertToBinaryString(buffer.getChar());
                    intervalStatuses.add(intervalStatus);
                    intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));
                }
                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(MINUTE, minuteInterval);
            }
        }

        interval.setChannelStatus(channelStatuses);
        interval.setChannelStatusDesc(channelStatusesDesc);
        interval.setIntervalStatus(intervalStatuses);
        interval.setIntervalStatusDesc(intervalStatusesDesc);
        interval.setReading(readings);
        interval.setReadingDate(readingDates);

        //next reading start date for next interval
        if (readingDates.size() != 0) {
            format.parse(readingDates.get(readingDates.size() - 1));
            cal = format.getCalendar();
            cal.add(MINUTE, minuteInterval);
            intervalStartDateForChannel = format.format(cal.getTime());
        }

        return interval;
    }


    private TrailerRecord readTrailer(byte[] record) {
        TrailerRecord trailerRecord = new TrailerRecord();

        trailerRecord.setRecordLength(parseInt(0, 1, record));
        trailerRecord.setRecordCode(parseInt(2, 3, record));
        trailerRecord.setTotalRecordCount(parseText(34, 43, record));
        trailerRecord.setTimestamp(parseText(204, 215, record));

        return trailerRecord;
    }

    private static String getStatusDesc(String binaryStatus, String statusType) {

        String reversed = new StringBuffer(binaryStatus).reverse().toString();
        StringBuilder statusDesc = new StringBuilder();

        // need to reverse so position in array is equal to bit position
        // for easier reference to the mdef specification

        char[] b = reversed.toCharArray();
        String statusDescriptionStr;

        for (int i = 0; i < b.length; i++) {
            if (b[i] == '1') {
                if (statusType.equals("INTERVAL")) {
                    statusDesc.append(IntervalStatus.getShortName(i)).append(STATUS_SEPARATOR);
                } else if (statusType.equals("CHANNEL")) {
                    statusDesc.append(ChannelStatus.getShortName(i)).append(STATUS_SEPARATOR);
                }
            }
        }

        statusDescriptionStr = statusDesc.toString();

        if (statusDescriptionStr.endsWith(STATUS_SEPARATOR) && statusDescriptionStr.contains(STATUS_SEPARATOR)) {
            statusDescriptionStr = statusDescriptionStr.substring(0, statusDescriptionStr.lastIndexOf(STATUS_SEPARATOR));
        }

        return statusDescriptionStr;
    }

    private boolean compareHourlyAndNormalDates(String twelveHourFormat, String twentyFourHourFormat) {

        boolean match = false;

        if (twelveHourFormat.length() == 12 && twentyFourHourFormat.length() == 12) {
            try {
                Date dateTwelve = new SimpleDateFormat("yyyyMMdd", ENGLISH).parse(twentyFourHourFormat.substring(0, 8));
                Calendar a = Calendar.getInstance();
                a.setTime(dateTwelve);
                a.add(DATE, 1);
                dateTwelve = a.getTime();

                Date dateTwentyFour = new SimpleDateFormat("yyyyMMdd", ENGLISH).parse(twelveHourFormat.substring(0, 8));
                Calendar b = Calendar.getInstance();
                b.setTime(dateTwentyFour);
                dateTwentyFour = a.getTime();

                match = (dateTwelve.equals(dateTwentyFour) &&
                        twelveHourFormat.substring(8, 12).equalsIgnoreCase("0000") &&
                        twentyFourHourFormat.substring(8, 12).equalsIgnoreCase("2400"));

            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        return match;
    }

}

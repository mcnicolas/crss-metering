package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.constants.UnitOfMeasure;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.parser.ParserUtil.parseText;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Calendar.MINUTE;

@Slf4j
public class MeterQuantityMDEFReader implements QuantityReader {

    private static final int RECORD_BLOCK_SIZE = 216;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");

    private String intervalStartDateForChannel = "";

    private int minuteInterval = 15;
    private static final int MINUTES_IN_HOUR = 60;

    // TODO: UGLY CODE. NEED TO OPTIMIZE!
    @Override
    public MeterData readData(InputStream inputStream) throws IOException {
        MDEFMeterData mdefMeterData = readMDEF(inputStream);

        Map<String, MeterDataDetail> meterDataMap = new HashMap<>();

        for (MDEFChannelHeader channel : mdefMeterData.getChannels()) {
            int intervalPerHour = MINUTES_IN_HOUR / channel.getIntervalPerHour();

            for (MDEFIntervalData interval : channel.getIntervals()) {
                List<Float> meterReadingList = interval.getMeterReading();
                List<Integer> channelStatusList = interval.getChannelStatus();
                List<Integer> intervalStatusList = interval.getIntervalStatus();
                List<String> readingDateList = interval.getReadingDate();

                for (int i = 0; i < meterReadingList.size(); i++) {
                    String key = interval.getCustomerID() + "_" + readingDateList.get(i);

                    MeterDataDetail value;
                    if (meterDataMap.containsKey(key)) {
                        value = meterDataMap.get(key);
                    } else {
                        value = new MeterDataDetail();

                        value.setSein(interval.getCustomerID());
                        value.setInterval(intervalPerHour);

                        try {
                            value.setReadingDateTime(dateFormat.parse(readingDateList.get(i)));
                        } catch (ParseException e) {
                            // TODO: Set date to Jan 1, 1900 00:00?
                            log.warn(e.getMessage(), e);
                        }

                        meterDataMap.put(key, value);
                    }

                    Double meterReading = Double.valueOf(meterReadingList.get(i));

                    int channelStatus = channelStatusList.get(i);
                    int intervalStatus = intervalStatusList.get(i);

                    UnitOfMeasure uom = UnitOfMeasure.fromCode(channel.getMeterNo());
                    switch (uom) {
                        case KWD:
                            value.setKwd(meterReading);
                            value.setKwdChannelStatus(channelStatus);
                            value.setKwdIntervalStatus(intervalStatus);
                            break;
                        case KWHD:
                            value.setKwhd(meterReading);
                            value.setKwhdChannelStatus(channelStatus);
                            value.setKwhdIntervalStatus(intervalStatus);
                            break;
                        case KVARHD:
                            value.setKvarhd(meterReading);
                            value.setKvarhdChannelStatus(channelStatus);
                            value.setKvarhdIntervalStatus(intervalStatus);
                            break;
                        case KWR:
                            value.setKwr(meterReading);
                            value.setKwrChannelStatus(channelStatus);
                            value.setKwrIntervalStatus(intervalStatus);
                            break;
                        case KWHR:
                            value.setKwhr(meterReading);
                            value.setKwhrChannelStatus(channelStatus);
                            value.setKwhrIntervalStatus(intervalStatus);
                            break;
                        case KVARHR:
                            value.setKvarhr(meterReading);
                            value.setKvarhrChannelStatus(channelStatus);
                            value.setKvarhrIntervalStatus(intervalStatus);
                            break;
                        case VAN:
                            value.setVan(meterReading);
                            value.setVanChannelStatus(channelStatus);
                            value.setVanIntervalStatus(intervalStatus);
                            break;
                        case VBN:
                            value.setVbn(meterReading);
                            value.setVbnChannelStatus(channelStatus);
                            value.setVbnIntervalStatus(intervalStatus);
                            break;
                        case VCN:
                            value.setVcn(meterReading);
                            value.setVcnChannelStatus(channelStatus);
                            value.setVcnIntervalStatus(intervalStatus);
                            break;
                        case IAN:
                            value.setIan(meterReading);
                            value.setIanChannelStatus(channelStatus);
                            value.setIanIntervalStatus(intervalStatus);
                            break;
                        case IBN:
                            value.setIbn(meterReading);
                            value.setIbnChannelStatus(channelStatus);
                            value.setIbnIntervalStatus(intervalStatus);
                            break;
                        case ICN:
                            value.setIcn(meterReading);
                            value.setIcnChannelStatus(channelStatus);
                            value.setIcnIntervalStatus(intervalStatus);
                            break;
                        case PF:
                            value.setPf(meterReading);
                            value.setPfChannelStatus(channelStatus);
                            value.setPfIntervalStatus(intervalStatus);
                            break;
                    }
                }
            }
        }

        List<MeterDataDetail> meterDataDetails = new ArrayList<>(meterDataMap.values());
        meterDataDetails.sort(Comparator.comparing(MeterDataDetail::getReadingDateTime));

        MeterData retVal = new MeterData();
        retVal.setDetails(meterDataDetails);

        return retVal;
    }

    public MDEFMeterData readMDEF(InputStream inputStream) throws IOException {

        MDEFMeterData MDEFMeterData = new MDEFMeterData();

        try (InputStream data = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[RECORD_BLOCK_SIZE];

            String channelHeaderTaStop = "";

            MDEFChannelHeader MDEFChannelHeader = null;

            while (data.read(buffer, 0, RECORD_BLOCK_SIZE) >= 0) {

                RecordCode recordCode = RecordCode.getRecordCode(buffer);

                // TODO: Change to strategy pattern
                switch (recordCode) {
                    case METER_HEADER:
                        MDEFChannelHeader = null;

                        MDEFMeterData.setMDEFHeader(readMeterHeader(buffer));

                        break;
                    case CHANNEL_HEADER:
                        MDEFChannelHeader = readChannelHeader(buffer);
                        MDEFMeterData.addChannel(MDEFChannelHeader);

                        intervalStartDateForChannel = MDEFChannelHeader.getStartTime();
                        channelHeaderTaStop = MDEFChannelHeader.getStopTime();
                        int channelIntervalPerHour = MDEFChannelHeader.getIntervalPerHour();

                        minuteInterval = MINUTES_IN_HOUR / channelIntervalPerHour;

                        break;
                    case INTERVAL_DATA:
                        MDEFIntervalData MDEFIntervalData = readIntervalData(buffer, intervalStartDateForChannel, channelHeaderTaStop);

                        if (MDEFIntervalData != null && MDEFChannelHeader != null) {
                            MDEFChannelHeader.addInterval(MDEFIntervalData);
                        }

                        break;
                    case TRAILER_RECORD:
                    default:
                        MDEFMeterData.setMDEFTrailerRecord(readTrailer(buffer));
                }
            }

            return MDEFMeterData;
        } catch (IOException e) {
            log.error(e.getMessage(), e);

            throw e;
        }
    }

    private MDEFHeader readMeterHeader(byte[] record) {
        MDEFHeader header = new MDEFHeader();

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

    private MDEFChannelHeader readChannelHeader(byte[] record) {
        MDEFChannelHeader retVal = new MDEFChannelHeader();

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

        retVal.setChannelStatusPresent(parseText(buffer, 1).equals("Y"));
        retVal.setIntervalStatusPresent(parseText(buffer, 1).equals("Y"));

        retVal.setStartMeterReading(parseText(buffer, 12));
        retVal.setStopMeterReading(parseText(buffer, 12));
        retVal.setMeterMultiplier(parseText(buffer, 1, 10));
        retVal.setServerType(parseText(buffer, 30, 1));

        retVal.setIntervalPerHour(Integer.parseInt(parseText(buffer, 10, 2)));
        retVal.setValidationResults(parseText(buffer, 14, 2));
        retVal.setPowerFlowDirection(parseText(buffer, 15, 1));
        retVal.setKvaSet(buffer.getShort());
        retVal.setDataOrigin(parseText(buffer, 1));

        return retVal;
    }

    // TODO: Use of intervalStartDateForChannel has a side-effect. Need to refactor
    private MDEFIntervalData readIntervalData(byte[] buffer, String intervalStartDate, String channelHeaderTaStop) {
        MDEFIntervalData retVal = new MDEFIntervalData();

        List<Float> meterReadings = new ArrayList<>();
        List<Integer> channelStatusList = new ArrayList<>();
        List<Integer> intervalStatusList = new ArrayList<>();
        List<String> readingDates = new ArrayList<>();

        DateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

        String readingDate = "";

        // TODO: Need to inspect this
        try {
            format.parse(intervalStartDate);
        } catch (ParseException e) {
            // TODO: Set value to a special field like -999?
            log.warn(e.getMessage(), e);
        }

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

            if (byteBuffer.hasRemaining()) {
                channelStatusList.add((int) byteBuffer.getChar());
            }

            if (byteBuffer.hasRemaining()) {
                intervalStatusList.add((int) byteBuffer.getChar());
            }

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
            try {
                format.parse(readingDates.get(readingDates.size() - 1));
            } catch (ParseException e) {
                log.warn(e.getMessage(), e);
            }

            cal = format.getCalendar();
            cal.add(MINUTE, minuteInterval);
            intervalStartDateForChannel = format.format(cal.getTime());
        }

        return retVal;
    }

    private MDEFTrailerRecord readTrailer(byte[] record) {
        MDEFTrailerRecord trailerRecord = new MDEFTrailerRecord();

        ByteBuffer buffer = ByteBuffer.wrap(record, 0, RECORD_BLOCK_SIZE);
        buffer.order(LITTLE_ENDIAN);

        trailerRecord.setRecordLength(buffer.getShort());
        trailerRecord.setRecordCode(buffer.getShort());
        trailerRecord.setTotalRecordCount(parseText(buffer, 30, 10));
        trailerRecord.setTimestamp(parseText(buffer, 160, 12));

        return trailerRecord;
    }

}

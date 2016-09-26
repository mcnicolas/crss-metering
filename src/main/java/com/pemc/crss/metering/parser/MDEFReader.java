package com.pemc.crss.metering.parser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.pemc.crss.metering.parser.ByteParser.parse;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Calendar.MINUTE;
import static org.apache.commons.io.IOUtils.closeQuietly;

public class MDEFReader {

    private static final Logger LOG = LoggerFactory.getLogger(MDEFReader.class);

    private static final int RECORD_LENGTH = 216;

    private static final String START_RECORD_MARKER = "1";
    private static final String CHANNEL_RECORD_MARKER = "10";
    private static final String TRAILER_RECORD_MARKER = "9999";

    private String intervalStartDateForChannel = "";
    private String channelHeaderTaStop = "";

    /* set default value of 4 intervals per hour: 15-minute interval */
    private int channelIntervalPerHour = 4;
    private int minuteInterval = 15;
    private static final int MINUTES_IN_HOUR = 60;

    public MeterHeader readMeterHeader(byte[] record) {
        MeterHeader meterHeader = new MeterHeader();

        meterHeader.setRecordLength(parse(0, 1, 'i', record));
        meterHeader.setRecordCode(parse(2, 3, 'i', record));
        meterHeader.setCustomerID(parse(4, 23, 'c', record));
        meterHeader.setCustomerName(parse(24, 43, 'c', record));
        meterHeader.setCustomerAddressLine1(parse(44, 63, 'c', record));
        meterHeader.setCustomerAddressLine2(parse(64, 83, 'c', record));
        meterHeader.setCustomerAccountNumber(parse(84, 103, 'c', record));
        meterHeader.setCustomerTotalChannels(parse(111, 114, 'c', record));
        meterHeader.setStartTime(parse(119, 130, 'c', record));
        meterHeader.setStopTime(parse(131, 142, 'c', record));
        meterHeader.setDstFlag(parse(143, 143, 'c', record));

        return meterHeader;
    }

    public ChannelHeader readChannelHeader(byte[] record) {
        ChannelHeader channelHeader = new ChannelHeader();

        channelHeader.setRecordLength(parse(0, 1, 'i', record));
        channelHeader.setRecordCode(parse(2, 3, 'i', record));
        channelHeader.setCustomerID(parse(4, 23, 'c', record));
        channelHeader.setRecorderID(parse(24, 37, 'c', record));
        channelHeader.setMeterNo(parse(44, 55, 'c', record));
        channelHeader.setStartTime(parse(56, 67, 'c', record));
        channelHeader.setStopTime(parse(68, 79, 'c', record));
        channelHeader.setMeterChannelNo(parse(93, 94, 'c', record));
        channelHeader.setCustomerChannelNo(parse(95, 96, 'i', record));
        channelHeader.setUomCode(parse(97, 98, 'c', record));
        channelHeader.setChannelStatusPresent(parse(99, 99, 'c', record));
        channelHeader.setIntervalStatusPresent(parse(100, 100, 'c', record));
        channelHeader.setStartMeterReading(parse(101, 112, 'c', record));
        channelHeader.setStopMeterReading(parse(113, 124, 'c', record));
        channelHeader.setMeterDialMultiplier(parse(126, 135, 'c', record));

//		String dcServerType = parse(166, 166, 'c', record);
//		if (dcServerType != null && dcServerType.length() >= 1) {
//			char DC_SERVTYPE = dcServerType.charAt(0);
//			if (DC_SERVTYPE == 'W' || DC_SERVTYPE == 'D'
//					|| DC_SERVTYPE == '+' || DC_SERVTYPE == '-') {
//				channelHeader.setServerType(dcServerType);
//			}
//		}

        channelHeader.setServerType(parse(166, 166, 'c', record));
        channelHeader.setIntervalPerHour(parse(177, 178, 'c', record));
        channelHeader.setValidationResults(parse(193, 194, 'c', record));
        channelHeader.setPowerFlowDirection(parse(210, 210, 'c', record));
        channelHeader.setKvaSet(parse(211, 212, 'i', record));
        channelHeader.setDataOrigin(parse(213, 213, 'c', record));

        return channelHeader;
    }

    public IntervalData readIntervalData(byte[] record, String recordChannelStat, String recordIntervalStat,
                                         String intervalStartDate) throws Exception {

        final int size = 216;
        float reading = 0;
        IntervalData interval = new IntervalData();

        List<String> readings = new ArrayList<>();
        List<String> channelStatuses = new ArrayList<>();
        List<String> channelStatusesDesc = new ArrayList<>();

        List<String> intervalStatuses = new ArrayList<>();
        List<String> intervalStatusesDesc = new ArrayList<>();
        List<String> readingDates = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

        String readingDate = "";
        String channelStatus = "";
        String intervalStatus = "";

        format.parse(intervalStartDate);

		/*try {
            format.parse(intervalStartDate);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/

//        calendar = format.getCalendar();

        //round-up to the next quarter
        int unroundedMinutes = calendar.get(MINUTE);
        int mod = unroundedMinutes % minuteInterval;
        //int newMinutes = unroundedMinutes;

        if (mod != 0) {
            calendar.add(MINUTE, (minuteInterval - mod));
        }

		/*try {*/
        java.nio.ByteBuffer buff = java.nio.ByteBuffer.wrap(record, 0, size);
        buff.order(LITTLE_ENDIAN);

        interval.setRecordLength(Integer.toString((int) buff.getChar()));
        interval.setRecordCode(Integer.toString((int) buff.getChar()));
        interval.setCustomerID(parse(4, 23, 'c', record));

        buff.position(24);

        if (recordChannelStat.equals("N") && recordIntervalStat.equals("N")) {
            // channel and interval status absent
            while (buff.hasRemaining()) {
                //TODO: should compare by date not String?
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buff.getFloat();
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

                readingDate = format.format(calendar.getTime());
                readingDates.add(readingDate);
                calendar.add(MINUTE, minuteInterval);

            }

        } else if (recordChannelStat.equals("Y") && recordIntervalStat.equals("N")) {
            // channel status present and interval status absent
            while (buff.hasRemaining()) {
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buff.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                if (buff.hasRemaining()) {
                    channelStatus = convertToBinaryString(buff.getChar());
                    channelStatuses.add(channelStatus);
                    channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));
                }

                intervalStatus = "0000000000000000";
                intervalStatuses.add(intervalStatus);
                intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));

                readingDate = format.format(calendar.getTime());
                readingDates.add(readingDate);
                calendar.add(MINUTE, minuteInterval);
            }

        } else if (recordChannelStat.equals("N") && recordIntervalStat.equals("Y")) {
            // channel status absent and interval status present
            while (buff.hasRemaining()) {
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buff.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                channelStatus = "0000000000000000";
                channelStatuses.add(channelStatus);
                channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));

                if (buff.hasRemaining()) {
                    intervalStatus = convertToBinaryString(buff.getChar());
                    intervalStatuses.add(intervalStatus);
                    intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));
                }

                readingDate = format.format(calendar.getTime());
                readingDates.add(readingDate);
                calendar.add(MINUTE, minuteInterval);
            }

        } else {
            // channel status present and interval status present
            while (buff.hasRemaining()) {
                //ignore reading after tastop time. Reading after taStop most probably with trouble
                if (readingDate.equals(channelHeaderTaStop)) {
                    break;
                }

                try {
                    reading = buff.getFloat();
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
                    readings.add(String.valueOf(reading));
                }

                if (buff.hasRemaining()) {
                    channelStatus = convertToBinaryString(buff.getChar());
                    channelStatuses.add(channelStatus);
                    channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));
                }

                if (buff.hasRemaining()) {
                    intervalStatus = convertToBinaryString(buff.getChar());
                    intervalStatuses.add(intervalStatus);
                    intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));
                }

                readingDate = format.format(calendar.getTime());
                readingDates.add(readingDate);
                calendar.add(MINUTE, minuteInterval);
            }

        }

		/*} catch (Exception e) {
			e.printStackTrace();
		}*/

        interval.setChannelStatus(channelStatuses);
        interval.setChannelStatusDesc(channelStatusesDesc);
        interval.setIntervalStatus(intervalStatuses);
        interval.setIntervalStatusDesc(intervalStatusesDesc);
        interval.setReading(readings);
        interval.setReadingDate(readingDates);

        //next reading start date for next interval
        if (readingDates.size() != 0) {
            format.parse(readingDates.get(readingDates.size() - 1));
			/*try {
				format.parse(readingDates.get(readingDates.size()-1).toString());
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
            calendar = format.getCalendar();
            calendar.add(MINUTE, minuteInterval);
            intervalStartDateForChannel = format.format(calendar.getTime());
        }

        return interval;
    }

    public TrailerRecord readTrailer(byte[] record) {
        TrailerRecord trailerRecord = new TrailerRecord();

        trailerRecord.setRecordLength(parse(0, 1, 'i', record));
        trailerRecord.setRecordCode(parse(2, 3, 'i', record));
        trailerRecord.setTotalRecordCount(parse(34, 43, 'c', record));
        trailerRecord.setTimestamp(parse(204, 215, 'c', record));

        return trailerRecord;
    }

    public MeterData readMDEF(File file) throws Exception {

        InputStream data = new BufferedInputStream(new FileInputStream(file));
        MeterData meterData = new MeterData();
        List<MeterHeader> meterHeaders = new ArrayList<>();

        byte[] buffer = new byte[RECORD_LENGTH];

        String recordIntervalStat = "N";
        String recordChannelStat = "N";
        String recordCode = "";
        String dcMeterid = "";

        try {
            MeterHeader meterMeterHeader = null;
            ChannelHeader channelHeader = null;
            IntervalData intervalData = null;

            while (data.read(buffer, 0, RECORD_LENGTH) >= 0) {

                recordCode = parse(2, 3, 'i', buffer);

                switch (recordCode) {
                    case START_RECORD_MARKER:
                        //re-initialize objects to null on meter header
//                    meterMeterHeader = null;
//                    channelHeader = null;
//                    intervalData  = null;

                        meterData.getMeterHeaders().add(readMeterHeader(buffer));

                        break;
                    case TRAILER_RECORD_MARKER:

                        meterData.setTrailerRecord(readTrailer(buffer));

                        break;
                    case CHANNEL_RECORD_MARKER:

                        channelHeader = readChannelHeader(buffer);

                        if (meterMeterHeader != null) {
                            meterMeterHeader.getChannels().add(channelHeader);
                        }

                    /*
                     * note down channel and interval status record availability
                     * which is going to be useful in following interval data record
                     */
                        recordIntervalStat = channelHeader.getIntervalStatusPresent();
                        recordChannelStat = channelHeader.getChannelStatusPresent();
                        intervalStartDateForChannel = channelHeader.getStartTime();
                        channelHeaderTaStop = channelHeader.getStopTime();
                        channelIntervalPerHour = Integer.valueOf(channelHeader.getIntervalPerHour());
                        minuteInterval = MINUTES_IN_HOUR / channelIntervalPerHour;
                        dcMeterid = channelHeader.getMeterNo();

                        if (intervalData != null && intervalData.getReadingDate().size() != 0) {
                            if (!intervalData.getReadingDate().get(intervalData.getReadingDate().size() - 1).equals(channelHeaderTaStop)) {
                                if (!compareHourlyAndNormalDates(intervalData.getReadingDate().get(intervalData.getReadingDate().size() - 1), channelHeaderTaStop)) {
                                    LOG.warn(file.getName() + " WARNING last interval reading date: " + intervalData.getReadingDate().get(intervalData.getReadingDate().size() - 1) + " not equal to channelHeaderTaStop: " + channelHeaderTaStop);

                                    /**
                                     * allow not equal channel header stop vs. generated interval stop time (there are cases like this, i.e, stop time for channel does not match # of interval records where
                                     * the geerated interval time depends)
                                     */
                                }
                            }
                        }

                        break;
                    default:

                        intervalData = readIntervalData(buffer, recordChannelStat, recordIntervalStat, intervalStartDateForChannel);
                        if (intervalData != null && channelHeader != null) {
                            channelHeader.getIntervals().add(intervalData);
                        }

                        break;
                }
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw e;
        } finally {
            closeQuietly(data);
        }

        if (meterHeaders.size() > 0) {
            meterData.setMeterHeaders(meterHeaders);
        }

        return meterData;
    }

    String convertToBinaryString(char a) {
        String binaryString = Integer.toBinaryString((int) a);
        binaryString = StringUtils.leftPad(binaryString, 16, "0");

        return binaryString;
    }

    static String getStatusDesc(String binaryStatus, String statusType) {

        String reversed = new StringBuffer(binaryStatus).reverse().toString();
        StringBuffer statusDesc = new StringBuffer();

        /**
         * need to reverse so position in array is equal to bit position
         * for easier reference to the mdef specification
         */
        char[] b = reversed.toCharArray();
        String descriptionSepator = "; ";
        String statusDescriptionStr = "";

        for (int i = 0; i < b.length; i++) {
            if (b[i] == '1') {
                if (statusType.equals("INTERVAL")) {
                    statusDesc.append(getIntervalStatMappingBit(i) + descriptionSepator);
                } else if (statusType.equals("CHANNEL")) {
                    statusDesc.append(getChannelStatMappingBit(i) + descriptionSepator);
                }
            }
        }

        statusDescriptionStr = statusDesc.toString();

        if (statusDescriptionStr != null && statusDescriptionStr.endsWith(descriptionSepator) && statusDescriptionStr.contains(descriptionSepator)) {
            statusDescriptionStr = statusDescriptionStr.substring(0, statusDescriptionStr.lastIndexOf(descriptionSepator));
        }

        return statusDescriptionStr;
    }

    static String getIntervalStatMappingBit(int bitPosition) {
        String desc = "";
        switch (bitPosition) {
            case 0:
//			desc = "Power Outage";
                desc = "PO";
                break;
            case 1:
//			desc = "Short Interval";
                desc = "SI";
                break;
            case 2:
//			desc = "Long Interval";
                desc = "LI";
                break;
            case 3:
//			desc = "CRC Error";
                desc = "CR";
                break;
            case 4:
//			desc = "RAM Checksum Error";
                desc = "RA";
                break;
            case 5:
//			desc = "ROM Checksum Error";
                desc = "RO";
                break;
            case 6:
//			desc = "Data Missing";
                desc = "LA";
                break;
            case 7:
//			desc = "Clock Error";
                desc = "CL";
                break;
            case 8:
//			desc = "Reset Occurred";
                desc = "BR";
                break;
            case 9:
//			desc = "Watchdog Time-out";
                desc = "WD";
                break;
            case 10:
//			desc = "Time Reset Occurred";
                desc = "TR";
                break;
            case 11:
//			desc = "Test Mode";
                desc = "TM";
                break;
            case 12:
//			desc = "Load Control";
                desc = "LC";
                break;
//		case 13:
//			desc = "Not Used";
//			break;
//		case 14:
//			desc = "Not Used";
//			break;
//		case 15:
//			desc = "Not Used";
//			break;
            default:
                desc = "";
                break;
        }

        return desc;

    }

    static String getChannelStatMappingBit(int bitPosition) {
        String desc = "";
        switch (bitPosition) {
            case 0:
//			desc = "Retransmitted / Updated Data";
                desc = "UPD";
                break;
            case 1:
//			desc = "Added Interval (Data Correction)";
                desc = "AD";
                break;
            case 2:
//			desc = "Replaced Interval (Data Correction)";
                desc = "RE";
                break;
            case 3:
//			desc = "Estimated Interval (Data Correction)";
                desc = "ES";
                break;
            case 4:
//			desc = "Pulse Overflow";
                desc = "POV";
                break;
            case 5:
//			desc = "Data Out Of Limits";
                desc = "DOV";
                break;
            case 6:
//			desc = "Excluded Data";
                desc = "ED";
                break;
            case 7:
//			desc = "Parity";
                desc = "PE";
                break;
            case 8:
//			desc = "Energy Type (Register Changed)";
                desc = "ETC";
                break;
            case 9:
//			desc = "Alarm";
                desc = "LR";
                break;
            case 10:
//			desc = "Harmonic Distortion";
                desc = "HD";
                break;
//		case 11:
//			desc = "Not Used";
//			break;
//		case 12:
//			desc = "Not Used";
//			break;
//		case 13:
//			desc = "Not Used";
//			break;
//		case 14:
//			desc = "Not Used";
//			break;
//		case 15:
//			desc = "Not Used";
//			break;
            default:
                desc = "";
                break;
        }

        return desc;

    }

    private boolean compareHourlyAndNormalDates(String twelveHourFormat, String twentyFourHourFormat) {

        boolean match = false;

        if (twelveHourFormat.length() == 12 && twentyFourHourFormat.length() == 12) {
            try {
                Date dateTwelve = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(twentyFourHourFormat.substring(0, 8));
                Calendar a = Calendar.getInstance();
                a.setTime(dateTwelve);
                a.add(Calendar.DATE, 1);
                dateTwelve = a.getTime();

                Date dateTwentyFour = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(twelveHourFormat.substring(0, 8));
                Calendar b = Calendar.getInstance();
                b.setTime(dateTwentyFour);
                dateTwentyFour = a.getTime();

                match = (dateTwelve.equals(dateTwentyFour) &&
                        twelveHourFormat.substring(8, 12).equalsIgnoreCase("0000") &&
                        twentyFourHourFormat.substring(8, 12).equalsIgnoreCase("2400") ? true : false);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return match;
    }

}

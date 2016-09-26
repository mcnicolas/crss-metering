package com.pemc.crss.metering.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

public class MDEFReader {

    String intervalStartDateForChannel = "";
    String channelHeaderTaStop = "";
    /* set default value of 4 intervals per hour: 15-minute interval */
    int channelIntervalPerHour = 4;
    int minuteInterval = 15;
    static final int MINUTES_IN_HOUR = 60;

    public Header readMtMeterHeader(byte[] record) {
        Header mHeader = new Header();
        mHeader.setrLen(transformByte(0, 1, 'i', record));
        mHeader.setrCode(transformByte(2, 3, 'i',record));
        mHeader.setCmCustid(transformByte(4, 23, 'c', record));
        mHeader.setCmName(transformByte(24, 43, 'c', record));
        mHeader.setCmAddr1(transformByte(44, 63, 'c', record));
        mHeader.setCmAddr2(transformByte(64, 83, 'c', record));
        mHeader.setCmAccount(transformByte(84, 103, 'c', record));
        mHeader.setCmLogchans(transformByte(111, 114, 'c', record));
        mHeader.setTaStart(transformByte(119, 130, 'c', record));
        mHeader.setTaStop(transformByte(131, 142, 'c', record));
        mHeader.setDstFlag(transformByte(143, 143, 'c', record));
        return mHeader;
    }

    public ChannelHeader readMtChannelHeader(byte[] record) {
        ChannelHeader cHeader = new ChannelHeader();
        cHeader.setrLen(transformByte(0, 1, 'i', record));
        cHeader.setrCode(transformByte(2, 3, 'i', record));
        cHeader.setDcCustid(transformByte(4, 23, 'c', record));
        cHeader.setDcRecid(transformByte(24, 37, 'c', record));
        cHeader.setDcMeterid(transformByte(44, 55, 'c', record));
        cHeader.setTaStart(transformByte(56, 67, 'c', record));
        cHeader.setTaStop(transformByte(68, 79, 'c', record));
        cHeader.setDcPyschan(transformByte(93, 94, 'c', record));
        cHeader.setDcLogchan(transformByte(95, 96, 'i', record));
        cHeader.setDcUmcode(transformByte(97, 98, 'c', record));
        cHeader.setChanStat(transformByte(99, 99, 'c', record));
        cHeader.setIntstat(transformByte(100, 100, 'c', record));
        cHeader.setStrtmtr(transformByte(101, 112, 'c', record));
        cHeader.setStopmtr(transformByte(113, 124, 'c', record));
        cHeader.setDcMmult(transformByte(126, 135, 'c', record));

//		String dcServerType = transformByte(166, 166, 'c', record);
//		if (dcServerType != null && dcServerType.length() >= 1) {
//			char DC_SERVTYPE = dcServerType.charAt(0);
//			if (DC_SERVTYPE == 'W' || DC_SERVTYPE == 'D'
//					|| DC_SERVTYPE == '+' || DC_SERVTYPE == '-') {
//				cHeader.setDcServerType(dcServerType);
//			}
//		}

        cHeader.setDcServerType(transformByte(166, 166, 'c', record));
        cHeader.setDrInphr(transformByte(177, 178, 'c', record));
        cHeader.setTdStatus(transformByte(193, 194, 'c', record));
        cHeader.setDcFlow(transformByte(210, 210, 'c', record));
        cHeader.setDcKvaset(transformByte(211, 212, 'i', record));
        cHeader.setTdOrigin(transformByte(213, 213, 'c', record));

        return cHeader;
    }

    public IntervalData readMtIntervalData(byte[] record, String recordChannelStat,
                                                String recordIntervalStat, String intervalStartDate) throws Exception {

        final int size = 216;
        float reading = 0;
        IntervalData interval = new IntervalData();

        List<String> readings = new ArrayList<String>();
        List<String> channelStatuses = new ArrayList<String>();
        List<String> channelStatusesDesc = new ArrayList<String>();

        List<String> intervalStatuses = new ArrayList<String>();
        List<String> intervalStatusesDesc = new ArrayList<String>();
        List<String> readingDates = new ArrayList<String>();

        Calendar cal=Calendar.getInstance();
        DateFormat format=new SimpleDateFormat("yyyyMMddHHmm");

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

        cal=format.getCalendar();

        //round-up to the next quarter
        int unroundedMinutes = cal.get(Calendar.MINUTE);
        int mod = unroundedMinutes % minuteInterval;
        //int newMinutes = unroundedMinutes;

        if (mod != 0) {
//			System.out.print("Rounding off minute orig = "+unroundedMinutes);
            cal.add(Calendar.MINUTE, (minuteInterval-mod));
//			System.out.println(" new = "+cal.get(Calendar.MINUTE));
        }

		/*try {*/
        java.nio.ByteBuffer buff = java.nio.ByteBuffer.wrap(record, 0, size);
        buff.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        interval.setrLen(new Integer((int) buff.getChar()).toString());
        interval.setrCode(new Integer((int) buff.getChar()).toString());
        interval.setCmCustId(transformByte(4, 23, 'c', record));

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
//						System.out.println("Reading NumberFormatException "+reading);
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
                cal.add(Calendar.MINUTE, minuteInterval);

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
//						System.out.println("Reading NumberFormatException "+reading);
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

                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(Calendar.MINUTE, minuteInterval);
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
//						System.out.println("Reading NumberFormatException "+reading);
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

                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(Calendar.MINUTE, minuteInterval);
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
//						System.out.println("interval rcode: "+interval.getrCode());
//						System.out.println("reading: "+reading);
                    readings.add(new BigDecimal(reading).toPlainString());
                } catch (NumberFormatException e) {
//						System.out.println("Reading NumberFormatException "+reading);
                    readings.add(String.valueOf(reading));

                }

                if (buff.hasRemaining()) {
                    channelStatus =convertToBinaryString(buff.getChar());
//						System.out.println("channelStatus: "+channelStatus);
                    channelStatuses.add(channelStatus);
                    channelStatusesDesc.add(getStatusDesc(channelStatus, "CHANNEL"));
                }

                if (buff.hasRemaining()) {
                    intervalStatus = convertToBinaryString(buff.getChar());
//						System.out.println("intervalStatus: "+intervalStatus);
                    intervalStatuses.add(intervalStatus);
                    intervalStatusesDesc.add(getStatusDesc(intervalStatus, "INTERVAL"));
                }
                readingDate = format.format(cal.getTime());
                readingDates.add(readingDate);
                cal.add(Calendar.MINUTE, minuteInterval);
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
        if (readingDates.size() != 0 ) {
            format.parse(readingDates.get(readingDates.size()-1).toString());
			/*try {
				format.parse(readingDates.get(readingDates.size()-1).toString());
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
            cal=format.getCalendar();
            cal.add(Calendar.MINUTE, minuteInterval);
            intervalStartDateForChannel = format.format(cal.getTime()) ;
        }

        return interval;
    }


    public TrailerRecord readMtTrailer(byte[] record) {
        TrailerRecord trailerRecord = new TrailerRecord();
        trailerRecord.setrLen(transformByte(0, 1, 'i', record));
        trailerRecord.setrCode(transformByte(2, 3, 'i', record));
        trailerRecord.setTotRec(transformByte(34, 43, 'c', record));
        trailerRecord.setXsTstamp(transformByte(204, 215, 'c', record));
        return trailerRecord;
    }

    public MeterData readMdef(File file) throws Exception {

        BufferedInputStream data = new BufferedInputStream(new FileInputStream(file));
        MeterData meterData = new MeterData();
        List<Header> headers = new ArrayList<>();

        int recordSize = 216;
        byte[] b = new byte[recordSize];

        String recordIntervalStat = "N";
        String recordChannelStat = "N";
        String dataRcode = "";
        String dcMeterid = "";

        try {
            Header  meterHeader = null;
            ChannelHeader channelHeader = null;
            IntervalData intervalData = null;

            while(data.read(b, 0, recordSize) >= 0) {

                dataRcode = transformByte(2, 3, 'i', b);

                if (dataRcode.equals("1")) {
                    //re-initialize objects to null on meter header
                    meterHeader = null;
                    channelHeader = null;
                    intervalData  = null;

                    meterHeader = readMtMeterHeader(b);
                    meterData.getHeaders().add(meterHeader);

                } else if (dataRcode.equals("9999")) {

                    TrailerRecord t = readMtTrailer(b);
                    meterData.setTrailerRecord(t);

                } else if (dataRcode.equals("10")) {

                    channelHeader = readMtChannelHeader(b);

                    if (meterHeader != null) {
                        meterHeader.getChannels().add(channelHeader);
                    }

					/*
					 * note down channel and interval status record availability
					 * which is going to be useful in following interval data record
					 */
                    recordIntervalStat = channelHeader.getIntstat();
                    recordChannelStat = channelHeader.getChanStat();
                    intervalStartDateForChannel = channelHeader.getTaStart();
                    channelHeaderTaStop = channelHeader.getTaStop();
                    channelIntervalPerHour = Integer.valueOf(channelHeader.getDrInphr());
                    minuteInterval = MINUTES_IN_HOUR/channelIntervalPerHour;
                    dcMeterid = channelHeader.getDcMeterid();

                    if (intervalData != null && intervalData.getReadingDate().size() != 0) {
                        if (! intervalData.getReadingDate().get(intervalData.getReadingDate().size()-1).equals(channelHeaderTaStop)) {
                            if (! compareHourlyAndNormalDates(intervalData.getReadingDate().get(intervalData.getReadingDate().size()-1), channelHeaderTaStop)) {
                                System.out.println(file.getName()+ " WARNING last interval reading date: "+intervalData.getReadingDate().get(intervalData.getReadingDate().size()-1) + " not equal to channelHeaderTaStop: "+channelHeaderTaStop);
                                /**
                                 * allow not equal channel header stop vs. generated interval stop time (there are cases like this, i.e, stop time for channel does not match # of interval records where
                                 * the geerated interval time depends)
                                 */
                            }
                        }
                    }

                } else {

                    intervalData = readMtIntervalData(b, recordChannelStat, recordIntervalStat,intervalStartDateForChannel);
                    if (intervalData != null && channelHeader != null) {
                        channelHeader.getIntervals().add(intervalData);
                    }

                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw e;
        } finally {
            if (data != null) {
                data.close();
            }
        }

        if (headers.size() > 0 ) {
            meterData.setHeaders(headers);
        }

        return meterData;
    }

    public String transformByte (int start, int end, char type, byte[] b) {

        String transformedData = "";
        char c;

        try {
            if (type == 'i') {
                /**
                 * byte to integer of 2 bytes, thus used char data type
                 * MDEF follows little-endian format. get Least Signicant Byte (LSB)
                 */
//				c = (char) (((char) b[start] & (char) 0x00FF) | (char) ((char) b[end] << 8));
//				transformedData = new Integer((int) c).toString();
//				System.out.println("version 1 "+transformedData);
                c = (char) ( (b[start] & 0x00FF) | Character.reverseBytes((char)b[end]));
                transformedData = new Integer((int) c).toString();
//				System.out.println("version 2 "+transformedData);
            } else if (type == 'c') {
                while (start <= end) {
                    transformedData = transformedData + (char) b[start];
                    start++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transformedData.trim();
    }

    String convertToBinaryString(char a) {
//		System.out.println("char a:" +(int)a);
        String binaryString = Integer.toBinaryString((int)a);
        binaryString = StringUtils.leftPad(binaryString, 16, "0");
//		System.out.println((int)a + " = "+binaryString);

        return binaryString;
    }

    static String getStatusDesc(String binaryStatus, String statusType) {

        String reversed = new StringBuffer(binaryStatus).reverse().toString();
        StringBuffer statusDesc = new StringBuffer();
        /**
         * need to reverse so position in array is equal to bit position
         * for easier reference to the mdef specification
         */
        char[] b =reversed.toCharArray();
        String descriptionSepator = "; ";
        String statusDescriptionStr = "";

        for (int i = 0; i < b.length; i++) {
//			System.out.println("b["+i+"] "+b[i]);
            if (b[i] == '1' ) {
                if (statusType.equals("INTERVAL")) {
                    statusDesc.append(getIntervalStatMappingBit(i)+descriptionSepator);
                } else if (statusType.equals("CHANNEL")) {
                    statusDesc.append(getChannelStatMappingBit(i)+descriptionSepator);
                }
            }
        }

        statusDescriptionStr =  statusDesc.toString();

        if (statusDescriptionStr != null && statusDescriptionStr.endsWith(descriptionSepator) && statusDescriptionStr.contains(descriptionSepator)) {
            statusDescriptionStr = statusDescriptionStr.substring(0,statusDescriptionStr.lastIndexOf(descriptionSepator));
        }

        return statusDescriptionStr;
    }

    static String getIntervalStatMappingBit(int bitPosition) {
//		System.out.println("bitPosition "+bitPosition);
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
//		System.out.println("returning desc "+desc);
        return desc;

    }

    static String getChannelStatMappingBit(int bitPosition) {
//		System.out.println("bitPosition "+bitPosition);
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
//		System.out.println("returning desc "+desc);
        return desc;

    }

    private boolean compareHourlyAndNormalDates(String twelveHourFormat, String twentyFourHourFormat) {

        boolean match = false;

        if(twelveHourFormat.length() == 12 && twentyFourHourFormat.length() == 12) {
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

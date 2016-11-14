package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqHeaderDataPair;
import com.pemc.crss.metering.parser.bcq.BcqInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.utils.DateTimeUtils;
import com.pemc.crss.metering.validator.exception.ValidationException;
import com.pemc.crss.metering.validator.util.BcqErrorMessageFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.pemc.crss.metering.constants.BcqValidationMessage.*;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.FIVE_MINUTES_PERIOD;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.QUARTERLY;

public class BcqDataValidator {

    public static List<BcqHeaderDataPair> getAndValidateRecord(List<List<String>> dataRecord, int intervalConfig,
                                                               Date validDeclarationDate) throws ValidationException {


        Set<BcqHeader> headerSet = new HashSet<>();
        List<BcqHeaderDataPair> headerDataPairList = new ArrayList<>();

        BcqInterval interval = getAndValidateInterval(dataRecord.get(0), intervalConfig);
        int validDataSize = getValidNoOfRecords(intervalConfig);

        for (int i = 2; i < dataRecord.size(); i ++) {
            int lineNo = i + 1;
            List<String> line = dataRecord.get(i);
            BcqHeader header = getHeader(line, validDeclarationDate, lineNo);
            BcqData prevData = null;
            BcqData data = getData(line, interval);

            if (!headerSet.contains(header)) {
                if (headerDataPairList.size() > 0) {
                    validateDataSize(headerDataPairList.get(headerDataPairList.size() - 1), validDataSize,
                            interval, intervalConfig);
                }

                headerDataPairList.add(new BcqHeaderDataPair(header, new ArrayList<>()));
                headerSet.add(header);
            }

            List<BcqData> currentDataList = headerDataPairList.get(headerDataPairList.size() - 1).getDataList();

            if (currentDataList.size() > 0) {
                prevData = currentDataList.get(currentDataList.size() - 1);
            }

            validateNextData(prevData, data, interval, lineNo);

            List<BcqData> dataList = divideDataByInterval(getData(line, interval), interval, intervalConfig);

            headerDataPairList.get(headerDataPairList.size() - 1).getDataList().addAll(dataList);
        }

        validateDataSize(headerDataPairList.get(headerDataPairList.size() - 1), validDataSize, interval, intervalConfig);

        return headerDataPairList;
    }

    private static List<BcqData> divideDataByInterval(BcqData data, BcqInterval interval, int intervalConfig) {
        List<BcqData> dividedDataList = new ArrayList<>();
        Date currentStartTime = data.getStartTime();
        float currentBcq = data.getBcq();
        int divisor;

        if (interval == QUARTERLY) {
            divisor = intervalConfig == 5 ? 3 : 1;
        } else {
            divisor = intervalConfig == 5 ? 12 : 4;
        }

        for (int count = 1; count <= divisor; count ++) {
            BcqData partialData = new BcqData();
            partialData.setReferenceMTN(data.getReferenceMTN());
            partialData.setStartTime(currentStartTime);
            partialData.setEndTime(new Date(currentStartTime.getTime() + TimeUnit.MINUTES.toMillis(intervalConfig)));
            partialData.setBcq(currentBcq / divisor);
            dividedDataList.add(partialData);

            currentStartTime = partialData.getEndTime();
        }

        return dividedDataList;
    }

    private static BcqInterval getAndValidateInterval(List<String> line, int intervalConfig)
            throws ValidationException {

        BcqInterval interval = BcqInterval.fromDescription(line.get(1));

        if (interval == BcqInterval.FIVE_MINUTES_PERIOD && intervalConfig != 5) {
            throw new ValidationException(
                    BcqErrorMessageFormatter.formatMessage(0, INVALID_INTERVAL.getMessage(),
                            interval.getDescription(), QUARTERLY.getDescription()));
        }

        return interval;
    }

    private static int getValidNoOfRecords(int intervalConfig) {

        return intervalConfig == 5 ?
                FIVE_MINUTES_PERIOD.getValidNoOfRecords() :
                QUARTERLY.getValidNoOfRecords();
    }

    private static BcqHeader getHeader(List<String> line, Date validDeclarationDate, int lineNo)
            throws ValidationException {

        BcqHeader header = new BcqHeader();
        System.out.println(line);
        Date declarationDate = BCQParserUtil.parseDateTime(line.get(3));

        validateDeclarationDate(declarationDate, validDeclarationDate, lineNo);

        if (DateTimeUtils.isStartOfDay(declarationDate)) {
            declarationDate = DateUtils.addDays(declarationDate, -1);
        } else {
            declarationDate = DateTimeUtils.startOfDay(declarationDate);
        }

        header.setSellingMTN(line.get(0));
        header.setBuyingParticipant(line.get(1));
        header.setDeclarationDate(declarationDate);
        return header;
    }

    private static BcqData getData(List<String> line, BcqInterval interval) {
        BcqData data = new BcqData();
        Date endTime = BCQParserUtil.parseDateTime(line.get(3));

        data.setReferenceMTN(line.get(2));
        data.setStartTime(getStartTime(endTime, interval));
        data.setEndTime(endTime);
        data.setBcq(Float.parseFloat(line.get(4)));

        return data;
    }

    private static Date getStartTime(Date date, BcqInterval interval) {
        return new Date(date.getTime() - interval.getTimeInMillis());
    }

    private static String formatAndGetDate(Date date, boolean withTime) {
        String format = withTime ? BCQParserUtil.DATE_TIME_FORMATS[0] : BCQParserUtil.DATE_FORMATS[0];
        DateFormat dateFormat = new SimpleDateFormat(format);

        return dateFormat.format(date);
    }

    private static void validateDeclarationDate(Date declarationDate, Date validDeclarationDate, int lineNo)
            throws ValidationException {

        Date today = new Date();
        Date tomorrow = DateTimeUtils.startOfDay(DateUtils.addDays(today, 1));
        Date yesterday = DateTimeUtils.startOfDay(DateUtils.addDays(today, -1));
        String validDeclarationDateString;

        if (validDeclarationDate == null) {
            validDeclarationDateString = StringUtils.<String>join(
                    Arrays.asList(formatAndGetDate(yesterday, true), formatAndGetDate(tomorrow, true)), " - ");

            if (declarationDate.after(tomorrow) || declarationDate.before(yesterday)) {
                String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo, INVALID_DATE.getMessage(),
                        formatAndGetDate(declarationDate, true), validDeclarationDateString);

                throw new ValidationException(errorMessage);
            }
        } else {
            validDeclarationDateString = StringUtils.<String>join(
                    Arrays.asList(
                            formatAndGetDate(DateTimeUtils.startOfDay(validDeclarationDate), true),
                            formatAndGetDate(DateTimeUtils.startOfDay(DateUtils.addDays(validDeclarationDate, 1)), true)),
                    " - ");

            if (declarationDate.after(validDeclarationDate) || declarationDate.before(validDeclarationDate)) {
                String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo, INVALID_DATE.getMessage(),
                        formatAndGetDate(declarationDate, true), validDeclarationDateString);

                throw new ValidationException(errorMessage);
            }
        }
    }

    private static void validateDataSize(BcqHeaderDataPair headerDataPair, int validDataSize,
                                         BcqInterval interval, int intervalConfig) throws ValidationException {

        int divisor;

        if (interval == FIVE_MINUTES_PERIOD) {
            divisor = intervalConfig == 5 ? 1 : 0;
        } else if (interval == QUARTERLY) {
            divisor = intervalConfig == 5 ? 3 : 1;
        } else {
            divisor = intervalConfig == 5 ? 12 : 4;
        }

        BcqHeader header = headerDataPair.getHeader();
        List<BcqData> lastDataList = headerDataPair.getDataList();

        if (lastDataList.size() != validDataSize) {
            String additionalMessage = String.format(
                    "Complete first the record with data (Selling MTN: %s, Buying Participant: %s, Date: %s), " +
                            "before end or proceeding to a new record.",
                    header.getSellingMTN(),
                    header.getBuyingParticipant(),
                    formatAndGetDate(header.getDeclarationDate(), false));

            String errorMessage = BcqErrorMessageFormatter.formatMessage(0,
                    INVALID_DATA_SIZE.getMessage() + " " + additionalMessage,
                    lastDataList.size() / divisor,
                    validDataSize / divisor);

            throw new ValidationException(errorMessage);
        }
    }

    private static void validateNextData(BcqData prevData, BcqData nextData, BcqInterval interval, int lineNo)
            throws ValidationException {

        Date validDate;
        long diff;

        if (prevData == null) {
            Date startOfDay = DateTimeUtils.startOfDay(nextData.getEndTime());
            diff = nextData.getEndTime().getTime() - startOfDay.getTime();
            validDate = DateUtils.addMilliseconds(startOfDay, (int) interval.getTimeInMillis());
        } else {
            diff = nextData.getEndTime().getTime() - prevData.getEndTime().getTime();
            validDate = DateUtils.addMilliseconds(prevData.getEndTime(), (int) interval.getTimeInMillis());
        }

        if (diff != interval.getTimeInMillis()) {
            String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo,
                    INVALID_DATE.getMessage(),
                    formatAndGetDate(nextData.getEndTime(), true),
                    formatAndGetDate(validDate, true));

            throw new ValidationException(errorMessage);
        }
    }
}

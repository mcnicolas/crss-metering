package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
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
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.pemc.crss.metering.constants.BcqValidationMessage.*;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.QUARTERLY;

public class BcqDataValidator {

    public static Map<BcqHeader, List<BcqData>> getAndValidateRecord(List<List<String>> dataRecord,
                                                                    int intervalConfig,
                                                                    Date validDeclarationDate) throws ValidationException {

        BcqInterval interval = null;
        Map<BcqHeader, List<BcqData>> headerDataMap = new HashMap<>();
        int currentLineNo = 1;
        BcqData lastData = null;

        for (List<String> line : dataRecord) {
            switch (currentLineNo) {
                case 1:
                    interval = BcqInterval.fromDescription(line.get(1));

                    if (interval == BcqInterval.FIVE_MINUTES_PERIOD && intervalConfig != 5) {
                        throw new ValidationException(
                                BcqErrorMessageFormatter.formatMessage(0, INVALID_INTERVAL,
                                        interval.getDescription(), QUARTERLY.getDescription()));
                    }
                    break;
                case 2:
                    break;
                default:
                    BcqHeader header = getHeader(line, validDeclarationDate, currentLineNo);

                    if (!headerDataMap.containsKey(header)) {
                        headerDataMap.put(header, new ArrayList<>());
                    }

                    List<BcqData> currentDataList = headerDataMap.get(header);

                    BcqData data = getData(line, interval);
                    BcqData prevData = null;

                    if (currentDataList.size() > 0) {
                        prevData = currentDataList.get(currentDataList.size() - 1);
                    } else {
                        if (lastData != null) {
                            if (!DateTimeUtils.isStartOfDay(lastData.getEndTime())) {
                                prevData = lastData;
                            }
                        }
                    }

                    validateNextData(prevData, data, interval, currentLineNo);

                    List<BcqData> dataList = divideDataByInterval(getData(line, interval), interval, intervalConfig);

                    headerDataMap.get(header).addAll(dataList);

                    lastData = data;
                    break;
            }

            currentLineNo ++;
        }

        validateDataSize(headerDataMap, intervalConfig);

        return headerDataMap;
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

    private static BcqHeader getHeader(List<String> line, Date validDeclarationDate, int lineNo)
            throws ValidationException {

        BcqHeader header = new BcqHeader();
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

    private static String formatAndGetDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(BCQParserUtil.DATE_TIME_FORMATS[0]);
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
                    Arrays.asList(formatAndGetDate(yesterday), formatAndGetDate(tomorrow)), " - ");

            if (declarationDate.after(tomorrow) || declarationDate.before(yesterday)) {
                String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo, INVALID_DATE,
                        formatAndGetDate(declarationDate), validDeclarationDateString);

                throw new ValidationException(errorMessage);
            }
        } else {
            validDeclarationDateString = StringUtils.<String>join(
                    Arrays.asList(
                            formatAndGetDate(DateTimeUtils.startOfDay(validDeclarationDate)),
                            formatAndGetDate(DateTimeUtils.startOfDay(DateUtils.addDays(validDeclarationDate, 1)))),
                    " - ");

            if (declarationDate.after(validDeclarationDate) || declarationDate.before(validDeclarationDate)) {
                String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo, INVALID_DATE,
                        formatAndGetDate(declarationDate), validDeclarationDateString);

                throw new ValidationException(errorMessage);
            }
        }
    }

    private static void validateDataSize(Map<BcqHeader, List<BcqData>> headerDataMap, int intervalConfig)
            throws ValidationException {

        int validDataSize = (intervalConfig == 5 ? 288 : 96) * headerDataMap.entrySet().size();
        int dataSize = 0;

        for (Entry<BcqHeader, List<BcqData>> entry : headerDataMap.entrySet()) {
            dataSize += entry.getValue().size();
        }

        if (dataSize != validDataSize) {
            String errorMessage = BcqErrorMessageFormatter.formatMessage(0, INVALID_DATA_SIZE, dataSize, validDataSize);

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
                    INVALID_DATE,
                    formatAndGetDate(nextData.getEndTime()),
                    formatAndGetDate(validDate));

            throw new ValidationException(errorMessage);
        }
    }
}

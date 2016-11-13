package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.parser.bcq.BcqInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.utils.DateTimeUtils;
import com.pemc.crss.metering.validator.exception.ValidationException;
import com.pemc.crss.metering.validator.util.BcqErrorMessageFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.pemc.crss.metering.constants.BcqValidationMessage.INVALID_END_TIME_FRAME;

public class BcqDataValidator {

    public static Map<BcqHeader, Set<BcqData>> getAndValidateRecord(Set<List<String>> dataRecord,
                                                                    int intervalConfig,
                                                                    Date validDeclarationDate) throws ValidationException {

        BcqInterval interval = null;
        Map<BcqHeader, Set<BcqData>> headerDataMap = new HashMap<>();
        int currentLineNo = 1;

        for (List<String> line : dataRecord) {

            switch (currentLineNo) {
                case 1:
                    interval = BcqInterval.fromDescription(line.get(1));
                    break;
                case 2:
                    break;
                default:
                    BcqHeader header = getHeader(line, validDeclarationDate, currentLineNo);

                    if (!headerDataMap.containsKey(header)) {
                        headerDataMap.put(header, new HashSet<>());
                    }

                    Set<BcqData> dataSet = divideDataByInterval(getData(line, interval), interval, intervalConfig);

                    headerDataMap.get(header).addAll(dataSet);
                    break;
            }

            currentLineNo ++;
        }

        return headerDataMap;
    }

    private static Set<BcqData> divideDataByInterval(BcqData data, BcqInterval interval, int intervalConfig) {
        Set<BcqData> dividedDataSet = new HashSet<>();
        Date currentStartTime = data.getStartTime();
        float currentBCQ = data.getBcq();
        int divisor;

        if (interval == BcqInterval.QUARTERLY) {
            divisor = intervalConfig == 15 ? 1 : 3;
        } else {
            divisor = intervalConfig == 15 ? 4 : 12;
        }

        for (int count = 1; count <= divisor; count ++) {
            BcqData partialData = new BcqData();
            partialData.setReferenceMTN(data.getReferenceMTN());
            partialData.setStartTime(currentStartTime);
            partialData.setEndTime(new Date(currentStartTime.getTime() + TimeUnit.MINUTES.toMillis(intervalConfig)));
            partialData.setBcq(currentBCQ / divisor);
            dividedDataSet.add(partialData);

            currentStartTime = partialData.getEndTime();
        }

        return dividedDataSet;
    }

    private static BcqHeader getHeader(List<String> line, Date validDeclarationDate, int lineNo)
            throws ValidationException {

        BcqHeader header = new BcqHeader();
        Date declarationDate = DateTimeUtils.startOfDay(BCQParserUtil.parseDateTime(line.get(2)));

        validateDeclarationDate(declarationDate, validDeclarationDate, lineNo);

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
        return DateFormatUtils.format(date, BCQParserUtil.DATE_FORMATS[0]);
    }

    private static void validateDeclarationDate(Date declarationDate, Date validDeclarationDate, int lineNo)
            throws ValidationException {

        Date today = new Date();
        Date yesterday = DateUtils.addDays(today, -1);
        String validDeclarationDateString;

        if (validDeclarationDate == null) {
            validDeclarationDateString = StringUtils.<String>join(
                    Arrays.asList(formatAndGetDate(today), formatAndGetDate(yesterday)), ",");

            if (declarationDate.after(today) || declarationDate.before(yesterday)) {
                String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo, INVALID_END_TIME_FRAME,
                        formatAndGetDate(declarationDate), validDeclarationDateString);

                throw new ValidationException(errorMessage);
            }
        } else {
            validDeclarationDateString = formatAndGetDate(validDeclarationDate);

            if (declarationDate.after(validDeclarationDate) || declarationDate.before(validDeclarationDate)) {
                String errorMessage = BcqErrorMessageFormatter.formatMessage(lineNo, INVALID_END_TIME_FRAME,
                        formatAndGetDate(declarationDate), validDeclarationDateString);

                throw new ValidationException(errorMessage);
            }
        }
    }
}

package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.bcq.BCQInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.validator.exception.ValidationException;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BCQValidator {

    private static final int NUMBER_OF_COLUMNS = 5;

    private BCQValidator() {}

    public static void validateInterval(String intervalString) throws ValidationException {
        if(BCQInterval.fromDescription(intervalString) == null) {
            throw new ValidationException("Interval is missing or not valid.");
        }
    }

    public static void validateLine(List<String> row, int currentLineNo) throws ValidationException {
        if (row.size() != NUMBER_OF_COLUMNS) {
            throw new ValidationException(String.format("Incorrect number of columns in line %d", currentLineNo));
        } else {
            validateSellingMTN(row.get(0), currentLineNo);
            validateBuyingParticipant(row.get(1), currentLineNo);
            validateReferenceMTN(row.get(2), currentLineNo);
            validateEndTime(row.get(3), currentLineNo);
            validateBCQ(row.get(4), currentLineNo);
        }
    }

    public static void validateNextData(List<BCQData> dataList, BCQData nextData, BCQInterval interval, int currentLineNo)
            throws ValidationException {
        long nextDataEndTime = nextData.getEndTime().getTime();
        long prevDataEndTime = removeTime(nextData.getEndTime()).getTime();

        if (dataList.size() > 0) {
            prevDataEndTime = dataList.get(dataList.size() - 1).getEndTime().getTime();
        }

        if((nextDataEndTime - prevDataEndTime) != interval.getTimeInMillis()) {
            throw new ValidationException(String.format(
                    "End time in line %d is not appropriate. Interval must be %s minutes.",
                    currentLineNo, TimeUnit.MINUTES.convert(interval.getTimeInMillis(), TimeUnit.MILLISECONDS)));
        }

        checkDuplicates(dataList, nextData);
    }

    private static void validateSellingMTN(String sellingMTN, int currentLineNo) throws ValidationException {
        if (sellingMTN == null) {
            throw new ValidationException(String.format("Selling MTN in line %d cannot be null.", currentLineNo));
        }
    }

    private static void validateBuyingParticipant(String buyingParticipant, int currentLineNo) throws ValidationException {
        if (buyingParticipant == null) {
            throw new ValidationException(String.format("Buying participant in line %d cannot be null.", currentLineNo));
        }
    }

    private static void validateReferenceMTN(String referenceMTN, int currentLineNo) throws ValidationException {
        if (referenceMTN == null) {
            throw new ValidationException(String.format("Reference MTN in line %d cannot be null.", currentLineNo));
        }
    }

    private static void validateEndTime(String endTime, int currentLineNo) throws ValidationException {
        if (endTime == null) {
            throw new ValidationException(String.format("End time in line %d cannot be null.", currentLineNo));
        } else {
            if (BCQParserUtil.parseDateTime(endTime) == null) {
                throw new ValidationException(String.format("End time in line %d is not a valid date.", currentLineNo));
            }
        }
    }

    private static void validateBCQ(String bcqString, int currentLineNo) throws ValidationException{
        if (bcqString == null) {
            throw new ValidationException(String.format("BCQ in line %d cannot be null.", currentLineNo));
        } else {
            if (!NumberUtils.isParsable(bcqString)) {
                throw new ValidationException(String.format("BCQ in line %d (%s) is not a valid BCQ.", currentLineNo, bcqString));
            }
        }
    }

    private static void checkDuplicates(List<BCQData> dataList, BCQData dataToAdd) throws ValidationException {
        int currentLineNo = 1;

        for (BCQData data: dataList) {
            if (data.getSellingMTN().equals(dataToAdd.getSellingMTN())
                    && data.getBuyingParticipant().equals(dataToAdd.getBuyingParticipant())
                    && data.getEndTime().equals(dataToAdd.getEndTime())) {
                throw new ValidationException(
                        String.format("Line %d contains duplicate values with line %d",
                                currentLineNo, dataList.size() + 1));
            }

            currentLineNo ++;
        }
    }

    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

}

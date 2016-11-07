package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.bcq.BCQInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.validator.exception.ValidationException;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BCQValidator {

    private static final int NUMBER_OF_COLUMNS = 5;

    private BCQValidator() {}

    public static void validateInterval(String intervalString) throws ValidationException {
        if(BCQInterval.fromDescription(intervalString) == null) {
            throw new ValidationException("Interval is missing or not valid.");
        }
    }

    public static void validateCsvRow(List<String> row, int currentRow) throws ValidationException {
        if (row.size() != NUMBER_OF_COLUMNS) {
            throw new ValidationException(String.format("Incorrect number of columns in row %d", currentRow));
        } else {
            validateSellingMTN(row.get(0), currentRow);
            validateBuyingParticipant(row.get(1), currentRow);
            validateReferenceMTN(row.get(2), currentRow);
            validateEndTime(row.get(3), currentRow);
            validateBCQ(row.get(4), currentRow);
        }
    }

    public static void validateNextData(List<BCQData> dataList, BCQData nextData, BCQInterval interval)
            throws ValidationException {
        long nextDataEndTime = nextData.getEndTime().getTime();
        long prevDataEndTime = removeTime(nextData.getEndTime()).getTime();

        if (dataList.size() > 0) {
            prevDataEndTime = dataList.get(dataList.size() - 1).getEndTime().getTime();
        }

        if((nextDataEndTime - prevDataEndTime) != interval.getTimeInMillis()) {
            throw new ValidationException(String.format(
                    "End time of row %d is not appropriate. Interval must be %s.",
                    dataList.size() + 1, interval.getDescription()));
        }

        checkDuplicates(dataList, nextData);
    }

    private static void validateSellingMTN(String sellingMTN, int currentRow) throws ValidationException {
        if (sellingMTN == null) {
            throw new ValidationException(String.format("Selling MTN of row %d cannot be null.", currentRow));
        }
    }

    private static void validateBuyingParticipant(String buyingParticipant, int currentRow) throws ValidationException {
        if (buyingParticipant == null) {
            throw new ValidationException(String.format("Buying participant of row %d cannot be null.", currentRow));
        }
    }

    private static void validateReferenceMTN(String referenceMTN, int currentRow) throws ValidationException {
        if (referenceMTN == null) {
            throw new ValidationException(String.format("Reference MTN of row %d cannot be null.", currentRow));
        }
    }

    private static void validateEndTime(String endTime, int currentRow) throws ValidationException {
        if (endTime == null) {
            throw new ValidationException(String.format("End time of row %d cannot be null.", currentRow));
        } else {
            if (BCQParserUtil.parseDateTime(endTime) == null) {
                throw new ValidationException(String.format("End time of row %d is not a valid date.", currentRow));
            }
        }
    }

    private static void validateBCQ(String bcqString, int currentRow) throws ValidationException{
        if (bcqString == null) {
            throw new ValidationException(String.format("BCQ of row %d cannot be null.", currentRow));
        } else {
            if (!NumberUtils.isParsable(bcqString)) {
                throw new ValidationException(String.format("BCQ of row %d (%s) is not a valid BCQ.", currentRow, bcqString));
            }
        }
    }

    private static void checkDuplicates(List<BCQData> dataList, BCQData dataToAdd) throws ValidationException {
        int currentRow = 1;

        for (BCQData data: dataList) {
            if (data.getSellingMTN().equals(dataToAdd.getSellingMTN())
                    && data.getBuyingParticipant().equals(dataToAdd.getBuyingParticipant())
                    && data.getEndTime().equals(dataToAdd.getEndTime())) {
                throw new ValidationException(
                        String.format("Row %d contains duplicate values with row %d",
                                currentRow, dataList.size() + 1));
            }

            currentRow ++;
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

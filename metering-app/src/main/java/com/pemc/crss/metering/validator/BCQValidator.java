package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.bcq.BCQInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.validator.exception.ValidationException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BCQValidator {

    private static final String DATE_FORMAT = "MM-dd-yyyy HH:mm";
    private static final int VALID_N0_OF_COLUMNS = 5;

    private BCQValidator() {}

    public static void validateInterval(String intervalString) throws ValidationException {
        if (intervalString == null) {
            throw new ValidationException("Interval cannot be null.");
        } else {
            if(BCQInterval.fromDescription(intervalString) == null) {
                throw new ValidationException(String.format("Interval (%s) is not valid.", intervalString));
            }
        }
    }

    public static void validateNoOfRecords(List<BCQData> dataList, BCQInterval interval) throws ValidationException {
        BCQData data = dataList.get(0);

        if (dataList.size() != interval.getValidNoOfRecords()) {
            throw new ValidationException(String.format(
                    "Incorrect number of records (found: %d, valid: %d) for data with: " +
                    "Selling MTN: %s, Buying Participant: %s",
                    dataList.size(),
                    interval.getValidNoOfRecords(),
                    data.getSellingMTN(),
                    data.getBuyingParticipant()));
        }
    }

    public static void validateLine(List<String> row, int currentLineNo, long timeFrameMillis)
            throws ValidationException {
        if (row.size() != VALID_N0_OF_COLUMNS) {
            throw new ValidationException(String.format("Incorrect number of columns in line %d", currentLineNo));
        } else {
            validateSellingMTN(row.get(0), currentLineNo);
            validateBuyingParticipant(row.get(1), currentLineNo);
            validateReferenceMTN(row.get(2), currentLineNo);
            validateEndTime(row.get(3), currentLineNo, timeFrameMillis);
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

        checkDuplicates(dataList, nextData, currentLineNo);

        if((nextDataEndTime - prevDataEndTime) != interval.getTimeInMillis()) {
            throw new ValidationException(String.format(
                    "End time (%s) in line %d is not appropriate. Interval must be %s minutes.",
                    DateFormatUtils.format(nextDataEndTime, DATE_FORMAT),
                    currentLineNo,
                    TimeUnit.MINUTES.convert(interval.getTimeInMillis(), TimeUnit.MILLISECONDS)));
        }
    }

    private static void validateSellingMTN(String sellingMTN, int currentLineNo) throws ValidationException {
        if (sellingMTN == null) {
            throw new ValidationException(String.format("Selling MTN in line %d cannot be null.", currentLineNo));
        }
    }

    private static void validateBuyingParticipant(String buyingParticipant, int currentLineNo)
            throws ValidationException {
        if (buyingParticipant == null) {
            throw new ValidationException(String.format("Buying participant in line %d cannot be null.",
                    currentLineNo));
        }
    }

    private static void validateReferenceMTN(String referenceMTN, int currentLineNo) throws ValidationException {
        if (referenceMTN == null) {
            throw new ValidationException(String.format("Reference MTN in line %d cannot be null.", currentLineNo));
        }
    }

    private static void validateEndTime(String endTime, int currentLineNo, long timeFrameMillis)
            throws ValidationException {
        if (endTime == null) {
            throw new ValidationException(String.format("End time in line %d cannot be null.", currentLineNo));
        } else {
            Date parsedDate = BCQParserUtil.parseDateTime(endTime);
            if (parsedDate == null) {
                throw new ValidationException(String.format("End time (%s) in line %d is not a valid date.",
                        endTime, currentLineNo));
            } else {
                validateEndTimeWithTimeFrame(parsedDate, currentLineNo, timeFrameMillis);
            }
        }
    }

    private static void validateEndTimeWithTimeFrame(Date endTime, int currentLineNo, long timeFrameMillis)
            throws ValidationException {
        Date today = new Date();

        if (removeTime(endTime).getTime() - removeTime(today).getTime() > timeFrameMillis) {
            throw new ValidationException(String.format("End time in line %d cannot be later than tomorrow midnight.",
                    currentLineNo));
        } else {
            if (removeTime(today).getTime() - removeTime(endTime).getTime() > timeFrameMillis) {
                throw new ValidationException(
                        String.format("End time (%s) in line %d cannot be earlier than yesterday midnight.",
                                DateFormatUtils.format(endTime, DATE_FORMAT), currentLineNo));
            }
        }
    }

    private static void validateBCQ(String bcqString, int currentLineNo) throws ValidationException{
        if (bcqString == null) {
            throw new ValidationException(String.format("BCQ in line %d cannot be null.", currentLineNo));
        } else {
            if (!NumberUtils.isParsable(bcqString)) {
                throw new ValidationException(String.format("BCQ in line %d (%s) is not a valid BCQ.",
                        currentLineNo, bcqString));
            }
        }
    }

    private static void checkDuplicates(List<BCQData> dataList, BCQData dataToAdd, int currentLineNo)
            throws ValidationException {
        for (BCQData data: dataList) {
            if (data.getSellingMTN().equals(dataToAdd.getSellingMTN())
                    && data.getBuyingParticipant().equals(dataToAdd.getBuyingParticipant())
                    && data.getEndTime().equals(dataToAdd.getEndTime())) {
                throw new ValidationException(
                        String.format("Line %d is repeated. " +
                                "Selling MTN: %s, Buying Participant: %s, End Time: %s",
                                currentLineNo,
                                data.getSellingMTN(),
                                data.getBuyingParticipant(),
                                DateFormatUtils.format(data.getEndTime(), DATE_FORMAT)));
            }
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

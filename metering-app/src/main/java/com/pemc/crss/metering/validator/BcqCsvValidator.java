package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.constants.BcqValidationMessage;
import com.pemc.crss.metering.parser.bcq.BcqInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.validator.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.pemc.crss.metering.constants.BcqValidationMessage.*;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.getValidIntervals;
import static com.pemc.crss.metering.validator.util.BcqErrorMessageFormatter.formatMessage;

public class BcqCsvValidator {

    private final static int VALID_INTERVAL_NO_OF_COLUMNS = 2;
    private final static int VALID_DATA_NO_OF_COLUMNS = 5;

    private BcqCsvValidator() {}

    public static void validateCsv(Set<List<String>> dataRecord) throws ValidationException {
        int currentLineNo = 1;

        for (List<String> line :dataRecord) {
            switch (currentLineNo) {
                case 1:
                    validateInterval(line);
                    break;
                case 2:
                    validateHeader(line);
                    break;
                default:
                    validateData(line, currentLineNo);
                    break;
            }

            currentLineNo ++;
        }
    }

    private static void validateInterval(List<String> line) throws ValidationException {
        int lineNo = 1; //default first line

        validateNoOfColumns(line.size(), VALID_INTERVAL_NO_OF_COLUMNS, lineNo);

        String intervalString = line.get(1);

        if(BcqInterval.fromDescription(intervalString) == null) {
            String errorMessage = formatMessage(lineNo, INVALID_INTERVAL,
                    intervalString, getValidIntervals());

            throw new ValidationException(errorMessage);
        }
    }

    private static void validateHeader(List<String> line) throws ValidationException {
        int lineNo = 2; //default second line

        validateNoOfColumns(line.size(), VALID_DATA_NO_OF_COLUMNS, lineNo);
    }

    private static void validateData(List<String> line, int lineNo) throws ValidationException {
        validateNoOfColumns(line.size(), VALID_DATA_NO_OF_COLUMNS, lineNo);

        validateSellingMTN(line.get(0), lineNo);
        validateBuyingParticipant(line.get(1), lineNo);
        validateReferenceMTN(line.get(2), lineNo);
        validateEndDate(line.get(3), lineNo);
        validateBcq(line.get(4), lineNo);
    }

    private static void validateNoOfColumns(int noOfColumns, int validNoOfColumns, int lineNo)
            throws ValidationException {

        if (noOfColumns != validNoOfColumns) {
            String errorMessage = formatMessage(lineNo, INVALID_NO_OF_COLUMNS, noOfColumns, validNoOfColumns);

            throw new ValidationException(errorMessage);
        }
    }

    private static void validateSellingMTN(String sellingMTN, int lineNo) throws ValidationException {
        validateNotEmpty(sellingMTN, MISSING_SELLING_MTN, lineNo);
    }

    private static void validateBuyingParticipant(String buyingParticipant, int lineNo) throws ValidationException {
        validateNotEmpty(buyingParticipant, MISSING_BUYING_PARTICIPANT, lineNo);
    }

    private static void validateReferenceMTN(String referenceMTN, int lineNo) throws ValidationException {
        validateNotEmpty(referenceMTN, MISSING_REFERENCE_MTN, lineNo);
    }

    private static void validateEndDate(String endDateString, int lineNo) throws ValidationException {
        validateNotEmpty(endDateString, MISSING_END_TIME, lineNo);

        Date endDate = BCQParserUtil.parseDateTime(endDateString);

        if (endDate == null) {
            String errorMessage = formatMessage(lineNo, INVALID_END_TIME_FORMAT,
                    endDateString, StringUtils.join(BCQParserUtil.DATE_FORMATS, ", "));

            throw new ValidationException(errorMessage);
        }
    }

    private static void validateBcq(String bcqString, int lineNo) throws ValidationException {
        validateNotEmpty(bcqString, MISSING_BCQ, lineNo);

        if (!NumberUtils.isParsable(bcqString)) {
            String errorMessage = formatMessage(lineNo, INVALID_BCQ, bcqString);

            throw new ValidationException(errorMessage);
        }
    }

    private static void validateNotEmpty(String value, BcqValidationMessage validationMessage, int lineNo)
            throws ValidationException {

        if (value == null || value.isEmpty()) {
            String errorMessage = formatMessage(lineNo, validationMessage);

            throw new ValidationException(errorMessage);
        }
    }
}

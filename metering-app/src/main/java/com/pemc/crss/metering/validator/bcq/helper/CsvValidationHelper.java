package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.validator.bcq.validation.CsvValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqInterval.*;
import static com.pemc.crss.metering.constants.BcqValidationRules.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDateTime;
import static com.pemc.crss.metering.utils.DateTimeUtils.isStartOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static com.pemc.crss.metering.validator.bcq.validation.CsvValidation.emptyInst;
import static com.pemc.crss.metering.validator.bcq.validation.CsvValidation.from;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Component
public class CsvValidationHelper {

    private static final int SELLING_MTN_INDEX = 0;
    private static final int BILLING_ID_INDEX = 1;
    private static final int REFERENCE_MTN_INDEX = 2;
    private static final int DATE_INDEX = 3;
    private static final int BCQ_INDEX = 4;
    private static final int VALID_NO_OF_COLUMNS = 5;
    private static final int VALID_NO_OF_LINES = 2;
    private static final int START_LINE_OF_DATA = 2;

    public Validation<List<List<String>>> validCsv(int intervalConfig) {
        return validCsvFile()
                .and(nonEmpty())
                .and(validColumnHeaders())
                .and(noEmptyLines())
                .and(intervalIsSet())
                .and(validInterval(intervalConfig))
                .and(sellingMtnIsSet())
                .and(billingIdIsSet())
                .and(referenceMtnIsSet())
                .and(dateIsSet())
                .and(bcqIsSet())
                .and(validDate())
                .and(validBcq())
                .and(positiveBcq())
                .and(noDuplicates())
                .and(sameTradingDate());
    }

    /****************************************************
     * VALIDATIONS
     ****************************************************/
    private CsvValidation validCsvFile() {
        return from(csv -> csv != null, INVALID_CSV_FILE.getErrorMessage());
    }

    private CsvValidation nonEmpty() {
        return from(csv -> csv.size() > VALID_NO_OF_LINES, EMPTY.getErrorMessage());
    }

    private CsvValidation validColumnHeaders() {
        return from(csv -> !csv.get(1).stream().anyMatch(StringUtils::isBlank)
                && csv.get(1).size() == VALID_NO_OF_COLUMNS,
            INCORRECT_COLUMN_HEADER_COUNT.getErrorMessage());
    }

    private CsvValidation noEmptyLines() {
        return from(csv -> csv.stream().
                        allMatch(line -> !line.stream().allMatch(StringUtils::isBlank)),
                EMPTY_LINE.getErrorMessage());
    }

    private CsvValidation intervalIsSet() {
        return from(csv -> !isBlank(getInterval(csv)), MISSING_INTERVAL.getErrorMessage());
    }

    private CsvValidation validInterval(int intervalConfig) {
        CsvValidation validation = emptyInst();
        Predicate<List<List<String>>> predicate = csv -> {
            String intervalString = getInterval(csv);
            BcqInterval interval = fromDescription(intervalString);
            validation.setErrorMessage(format(INCORRECT_DECLARED_INTERVAL.getErrorMessage(), intervalString));

            return !(interval == null
                    || (intervalConfig == 5 && interval == QUARTERLY)
                    || (intervalConfig == 15 && interval == FIVE_MINUTES_PERIOD));
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private CsvValidation sellingMtnIsSet() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> isBlank(line.get(SELLING_MTN_INDEX))), MISSING_SELLING_MTN.getErrorMessage());
    }

    private CsvValidation billingIdIsSet() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> isBlank(line.get(BILLING_ID_INDEX))), MISSING_BILLING_ID.getErrorMessage());
    }

    private CsvValidation referenceMtnIsSet() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> isBlank(line.get(REFERENCE_MTN_INDEX))), MISSING_REFERENCE_MTN.getErrorMessage());
    }

    private CsvValidation dateIsSet() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> isBlank(line.get(DATE_INDEX))), MISSING_DATE.getErrorMessage());
    }

    private CsvValidation bcqIsSet() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> isBlank(line.get(BCQ_INDEX))), MISSING_BCQ.getErrorMessage());
    }

    private CsvValidation validDate() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> parseDateTime(line.get(DATE_INDEX)) == null), INCORRECT_DATE_FORMAT.getErrorMessage());
    }

    private CsvValidation validBcq() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> !isParsable(line.get(BCQ_INDEX))), INCORRECT_DATA_TYPE.getErrorMessage());
    }

    private CsvValidation positiveBcq() {
        return from(csv -> !getDataList(csv).stream().
                anyMatch(line -> new BigDecimal(line.get(BCQ_INDEX)).signum() == -1), NEGATIVE_BCQ.getErrorMessage());
    }

    private CsvValidation noDuplicates() {
        CsvValidation validation = emptyInst();
        Predicate<List<List<String>>> predicate =  csv -> {
            Set<List<String>> uniqueSet = new HashSet<>();
            return !csv.subList(2, csv.size()).stream().anyMatch(line -> {
                if (!uniqueSet.add(asList(line.get(SELLING_MTN_INDEX), line.get(BILLING_ID_INDEX),
                        line.get(DATE_INDEX)))) {

                    validation.setErrorMessage(format(DUPLICATE_DATE.getErrorMessage(), line.get(SELLING_MTN_INDEX),
                            line.get(BILLING_ID_INDEX),
                            line.get(DATE_INDEX)));
                    return true;
                }
                return false;
            });
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private CsvValidation sameTradingDate() {
        return from(csv -> {
            Date firstTradingDate = getTradingDate(getDataList(csv).get(0).get(DATE_INDEX));
            return !getDataList(csv).stream().
                    anyMatch(line -> !firstTradingDate.equals(getTradingDate(line.get(DATE_INDEX))));
        }, INVALID_TRADING_DATE.getErrorMessage());
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private String getInterval(List<List<String>> csv) {
        return csv.get(0).get(1);
    }

    private List<List<String>> getDataList(List<List<String>> csv) {
        return csv.subList(START_LINE_OF_DATA, csv.size());
    }

    private Date getTradingDate(String tradingDateString) {
        Date tradingDate = parseDateTime(tradingDateString);
        if (isStartOfDay(tradingDate)) {
            return tradingDate == null ? null : addDays(tradingDate, -1);
        } else {
            return startOfDay(tradingDate);
        }
    }

}

package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.google.common.collect.Lists;
import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.CsvValidationHelper;
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

import static com.pemc.crss.metering.constants.BcqInterval.FIVE_MINUTES_PERIOD;
import static com.pemc.crss.metering.constants.BcqInterval.QUARTERLY;
import static com.pemc.crss.metering.constants.BcqInterval.fromDescription;
import static com.pemc.crss.metering.constants.BcqValidationError.*;
import static com.pemc.crss.metering.constants.BcqValidationError.INVALID_TRADING_DATE;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDateTime;
import static com.pemc.crss.metering.utils.DateTimeUtils.isStartOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Component
public class CsvValidationHelperImpl implements CsvValidationHelper {

    private static final int SELLING_MTN_INDEX = 0;
    private static final int BILLING_ID_INDEX = 1;
    private static final int REFERENCE_MTN_INDEX = 2;
    private static final int DATE_INDEX = 3;
    private static final int BCQ_INDEX = 4;
    private static final int BUYER_MTN_INDEX = 5;
    private static final int VALID_NO_OF_COLUMNS = 6;
    private static final int VALID_NO_OF_LINES = 2;
    private static final int START_LINE_OF_DATA = 2;

    @Override
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
                .and(validBcqLength())
                .and(noDuplicates())
                .and(validateBuyerMtn());
                /*.and(sameTradingDate())*/
        // .and(buyerMtnIsSet());
    }

    private CsvValidation validCsvFile() {
        return new CsvValidation(csv -> csv != null, new BcqValidationErrorMessage(INVALID_CSV_FILE));
    }

    private CsvValidation nonEmpty() {
        return new CsvValidation(csv -> csv.size() > VALID_NO_OF_LINES, new BcqValidationErrorMessage(EMPTY));
    }

    private CsvValidation validColumnHeaders() {
        return new CsvValidation(csv -> csv.get(1).stream()
                .noneMatch(StringUtils::isBlank)
                && csv.get(1).size() == VALID_NO_OF_COLUMNS,
                new BcqValidationErrorMessage(INCORRECT_COLUMN_HEADER_COUNT));
    }

    private CsvValidation noEmptyLines() {
        return new CsvValidation(csv -> csv.stream()
                .noneMatch(line -> line.stream().allMatch(StringUtils::isBlank)),
                new BcqValidationErrorMessage(EMPTY_LINE));
    }

    private CsvValidation intervalIsSet() {
        return new CsvValidation(csv -> !isBlank(getInterval(csv)), new BcqValidationErrorMessage(MISSING_INTERVAL));
    }

    private CsvValidation validInterval(int intervalConfig) {
        CsvValidation validation = new CsvValidation();
        Predicate<List<List<String>>> predicate = csv -> {
            String intervalString = getInterval(csv);
            BcqInterval interval = fromDescription(intervalString);
            BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(INCORRECT_DECLARED_INTERVAL,
                    singletonList(intervalString));
            validation.setErrorMessage(errorMessage);
            return !(interval == null
                    || (intervalConfig == 5 && interval == QUARTERLY)
                    || (intervalConfig == 15 && interval == FIVE_MINUTES_PERIOD));
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private CsvValidation sellingMtnIsSet() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .noneMatch(line -> isBlank(line.get(SELLING_MTN_INDEX))),
                new BcqValidationErrorMessage(MISSING_SELLING_MTN));
    }

    private CsvValidation buyerMtnIsSet() {
        CsvValidation validation = new CsvValidation();
        Predicate<List<List<String>>> predicate = csv -> {
            List<List<String>> dataCsv = getDataList(csv);
            Long countEmpty = getCountEmptyBuyerMtn(dataCsv);
            Long countNonEmpty = getCountNonEmptyBuyerMtn(dataCsv);
            BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(MISSING_BUYER_MTN);
            validation.setErrorMessage(errorMessage);
            return (countEmpty == 0L || countNonEmpty == 0L);
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private CsvValidation billingIdIsSet() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .noneMatch(line -> isBlank(line.get(BILLING_ID_INDEX))),
                new BcqValidationErrorMessage(MISSING_BILLING_ID));
    }

    private CsvValidation referenceMtnIsSet() {
        return new CsvValidation(csv -> !getDataList(csv).stream()
                .anyMatch(line -> isBlank(line.get(REFERENCE_MTN_INDEX))),
                new BcqValidationErrorMessage(MISSING_REFERENCE_MTN));
    }

    private CsvValidation dateIsSet() {
        return new CsvValidation(csv -> !getDataList(csv).stream()
                .anyMatch(line -> isBlank(line.get(DATE_INDEX))),
                new BcqValidationErrorMessage(MISSING_DATE));
    }

    private CsvValidation bcqIsSet() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .noneMatch(line -> isBlank(line.get(BCQ_INDEX))),
                new BcqValidationErrorMessage(MISSING_BCQ));
    }

    private CsvValidation validDate() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .noneMatch(line -> parseDateTime(line.get(DATE_INDEX)) == null),
                new BcqValidationErrorMessage(INCORRECT_DATE_FORMAT));
    }

    private CsvValidation validBcq() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .allMatch(line -> isParsable(line.get(BCQ_INDEX))),
                new BcqValidationErrorMessage(INCORRECT_DATA_TYPE));
    }

    private CsvValidation positiveBcq() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .noneMatch(line -> new BigDecimal(line.get(BCQ_INDEX)).signum() == -1),
                new BcqValidationErrorMessage(NEGATIVE_BCQ));
    }

    private CsvValidation validBcqLength() {
        return new CsvValidation(csv -> getDataList(csv).stream()
                .noneMatch(line -> {
                    BigDecimal bcq = new BigDecimal(line.get(BCQ_INDEX));
                    bcq = bcq.stripTrailingZeros();
                    String integerPart = bcq.toPlainString();
                    if (integerPart.contains(".")) {
                        String[] bcqStrings = split(integerPart, ".");
                        integerPart = bcqStrings[0];
                        String fractionalPart = bcqStrings[1];
                        if (fractionalPart.length() > 9) {
                            return true;
                        }
                    }
                    return integerPart.length() > 19;
                }), new BcqValidationErrorMessage(INVALID_BCQ_LENGTH));
    }

    private CsvValidation noDuplicates() {
        CsvValidation validation = new CsvValidation();
        Predicate<List<List<String>>> predicate = csv -> {
            Set<List<String>> uniqueSet = new HashSet<>();
            return csv.subList(2, csv.size()).stream()
                    .noneMatch(line -> {
                        List<String> uniqueRow = asList(
                                line.get(SELLING_MTN_INDEX).toUpperCase(),
                                line.get(BILLING_ID_INDEX).toUpperCase(),
                                line.get(REFERENCE_MTN_INDEX).toUpperCase(),
                                line.get(DATE_INDEX));
                        if (!uniqueSet.add(uniqueRow)) {
                            BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(DUPLICATE_DATE,
                                    uniqueRow);
                            validation.setErrorMessage(errorMessage);
                            return true;
                        }
                        return false;
                    });
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private CsvValidation validateBuyerMtn() {
        CsvValidation validation = new CsvValidation();
        Predicate<List<List<String>>> predicate = csv -> {
            String refMtn = getDataList(csv).get(0).get(REFERENCE_MTN_INDEX);
            List<String> dateWithBuyerId = Lists.newArrayList();
            return getDataList(csv).stream()
                    .noneMatch(line -> {
                        if (line.get(REFERENCE_MTN_INDEX).equals(refMtn)) {
                            if (isNoneBlank(line.get(BUYER_MTN_INDEX))) {
                                dateWithBuyerId.add(line.get(DATE_INDEX));
                            }
                        } else {
                            if (dateWithBuyerId.contains(line.get(DATE_INDEX))) {
                                if (isBlank(line.get(BUYER_MTN_INDEX))) {
                                    BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(MISSING_BUYER_MTN);
                                    validation.setErrorMessage(errorMessage);
                                    return true;
                                }
                            } else {
                                if (isNoneBlank(line.get(BUYER_MTN_INDEX))) {
                                    BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(MISSING_BUYER_MTN);
                                    validation.setErrorMessage(errorMessage);
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private CsvValidation sameTradingDate() {
        return new CsvValidation(csv -> {
            Date firstTradingDate = getTradingDate(getDataList(csv).get(0).get(DATE_INDEX));
            String refMtn = getDataList(csv).get(0).get(REFERENCE_MTN_INDEX);
            return firstTradingDate != null
                    && getDataList(csv).stream()
                    .allMatch(line -> firstTradingDate.equals(getTradingDate(line.get(DATE_INDEX)))
                            && refMtn.equals(line.get(REFERENCE_MTN_INDEX)));
        }, new BcqValidationErrorMessage(INVALID_TRADING_DATE));
    }

    private String getInterval(List<List<String>> csv) {
        return csv.get(0).get(1);
    }

    private Long getCountEmptyBuyerMtn(List<List<String>> csv) {
        return csv.stream().filter(line -> isBlank(line.get(BUYER_MTN_INDEX))).count();
    }

    private Long getCountNonEmptyBuyerMtn(List<List<String>> csv) {
        return csv.stream().filter(line -> isNoneBlank(line.get(BUYER_MTN_INDEX))).count();
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

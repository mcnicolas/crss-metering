package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.*;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation.emptyInst;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Component
public class HeaderListValidationHelper {

    public Validation<List<BcqHeader>> validHeaderList(int declarationDateConfig, BcqInterval interval) {
        return openTradingDate(declarationDateConfig)
                .and(validDataSize(interval))
                .and(validTimeIntervals(interval));
    }

    public Validation<List<BcqHeader>> validHeaderList(Date tradingDate, BcqInterval interval) {
        return sameTradingDate(tradingDate).
                and(validDataSize(interval)).
                and(validTimeIntervals(interval));
    }

    private HeaderListValidation openTradingDate(int declarationDateConfig) {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date tradingDate = headerList.get(0).getTradingDate();
            Date today = new Date();
            Date minDate = startOfDay(addDays(today, -declarationDateConfig));
            Date maxDate = startOfDay(today);
            if (tradingDate.after(maxDate) || tradingDate.before(minDate)) {
                validation.setErrorMessage(new BcqValidationErrorMessage(CLOSED_TRADING_DATE,
                        singletonList(formatLongDate(tradingDate))));
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private HeaderListValidation sameTradingDate(Date tradingDate) {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date headerTradingDate = headerList.get(0).getTradingDate();
            if (tradingDate.equals(headerTradingDate)) {
                return true;
            }
            validation.setErrorMessage(new BcqValidationErrorMessage(DIFFERENT_TRADING_DATE));
            return false;
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private HeaderListValidation validDataSize(BcqInterval interval) {
        int validBcqSize = interval.getValidNoOfRecords();
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList ->
                headerList.stream().allMatch(header -> {
                    if (header.getDataList().size() == validBcqSize) {
                        return true;
                    }
                    BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(INCOMPLETE_ENTRIES,
                            asList(formatDate(header.getTradingDate()), header.getSellingMtn(), header.getBillingId(), String.valueOf(validBcqSize)));
                    validation.setErrorMessage(errorMessage);
                    return false;
                });
        validation.setPredicate(predicate);
        return validation;
    }

    private HeaderListValidation validTimeIntervals(BcqInterval interval) {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList ->
                headerList.stream()
                        .allMatch(header -> {
                            Date previousDate = null;
                            long diff;
                            for (BcqData data : header.getDataList()) {
                                if (previousDate == null) {
                                    Date startOfDay = startOfDay(data.getEndTime());
                                    diff = data.getEndTime().getTime() - startOfDay.getTime();
                                } else {
                                    diff = data.getEndTime().getTime() - previousDate.getTime();
                                }

                                if (diff == interval.getTimeInMillis()) {
                                    previousDate = data.getEndTime();
                                    continue;
                                }
                                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(
                                        INCORRECT_TIME_INTERVALS, asList(formatDateTime(data.getEndTime()),
                                        interval.getDescription()));
                                validation.setErrorMessage(errorMessage);
                                return false;
                            }
                            return true;
                        });
        validation.setPredicate(predicate);
        return validation;
    }

}

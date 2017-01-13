package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationRules.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDateTime;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDate;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation.emptyInst;
import static java.lang.String.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Component
public class HeaderListValidationHelper {

    public Validation<List<BcqHeader>> validHeaderList(int declarationDateConfig, BcqInterval interval) {
        return openTradingDate(declarationDateConfig).
                and(validDataSize(interval)).
                and(validTimeIntervals(interval));
    }

    public Validation<List<BcqHeader>> validHeaderList(Date tradingDate, BcqInterval interval) {
        return sameTradingDate(tradingDate).
                and(validDataSize(interval)).
                and(validTimeIntervals(interval));
    }

    private HeaderListValidation openTradingDate(int declarationDateConfig) {
        HeaderListValidation headerListValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date tradingDate = headerList.get(0).getTradingDate();
            Date today = new Date();
            Date minDate = startOfDay(addDays(today, -declarationDateConfig));
            Date maxDate = startOfDay(today);
            if (tradingDate.after(maxDate) || tradingDate.before(minDate)) {
                headerListValidation.setErrorMessage(format(CLOSED_TRADING_DATE.getErrorMessage(),
                        formatLongDate(tradingDate)));
                return false;
            }
            return true;
        };
        headerListValidation.setPredicate(predicate);
        return headerListValidation;
    }

    private HeaderListValidation sameTradingDate(Date tradingDate) {
        HeaderListValidation headerListValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date headerTradingDate = headerList.get(0).getTradingDate();
            if (!tradingDate.equals(headerTradingDate)) {
                headerListValidation.setErrorMessage(DIFFERENT_TRADING_DATE.getErrorMessage());
                return false;
            }
            return true;
        };
        headerListValidation.setPredicate(predicate);
        return headerListValidation;
    }

    private HeaderListValidation validDataSize(BcqInterval interval) {
        int validBcqSize = interval.getValidNoOfRecords();
        HeaderListValidation headerListValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList ->
                !headerList.stream().anyMatch(header -> {
                    if (header.getDataList().size() != validBcqSize) {
                        headerListValidation.setErrorMessage(format(INCOMPLETE_ENTRIES.getErrorMessage(),
                                formatDate(header.getTradingDate()),
                                header.getSellingMtn(),
                                header.getBillingId(),
                                validBcqSize));
                        return true;
                    }
                    return false;
                });
        headerListValidation.setPredicate(predicate);
        return headerListValidation;
    }

    private HeaderListValidation validTimeIntervals(BcqInterval interval) {
        HeaderListValidation headerListValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList ->
                !headerList.stream().anyMatch(header -> {
                    Date previousDate = null;
                    long diff;
                    for (BcqData data : header.getDataList()) {
                        if (previousDate == null) {
                            Date startOfDay = startOfDay(data.getEndTime());
                            diff = data.getEndTime().getTime() - startOfDay.getTime();
                        } else {
                            diff = data.getEndTime().getTime() - previousDate.getTime();
                        }

                        if (diff != interval.getTimeInMillis()) {
                            headerListValidation.setErrorMessage(format(INCORRECT_TIME_INTERVALS.getErrorMessage(),
                                    formatDateTime(data.getEndTime()),
                                    interval.getDescription()));
                            return true;
                        }
                        previousDate = data.getEndTime();
                    }
                    return false;
                });
        headerListValidation.setPredicate(predicate);
        return headerListValidation;
    }

}

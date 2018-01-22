package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.HeaderListValidationHelper;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDateTime;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDate;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Component
public class HeaderListValidationHelperImpl implements HeaderListValidationHelper {

    @Override
    public Validation<List<BcqHeader>> validHeaderList(int declarationDateConfig) {
        return validDataSize()
                .and(validTimeIntervals())
                .and(openTradingDate(declarationDateConfig));
    }

    @Override
    public Validation<List<BcqHeader>> validHeaderList(Date tradingDate) {
        return sameTradingDate(tradingDate).
                and(validDataSize()).
                and(validTimeIntervals());
    }

    private HeaderListValidation openTradingDate(int declarationDateConfig) {
        HeaderListValidation validation = new HeaderListValidation();
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
        HeaderListValidation validation = new HeaderListValidation();
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

    private HeaderListValidation validDataSize() {
        HeaderListValidation validation = new HeaderListValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> headerList.stream().allMatch(header -> {
            int validBcqSize = header.getInterval().getValidNoOfRecords() * (int)(header.getRefMtnSize().longValue());
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

    private HeaderListValidation validTimeIntervals() {
        HeaderListValidation validation = new HeaderListValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> headerList.stream().allMatch(header -> {
            BcqInterval interval = header.getInterval();
            Date previousDate = null;
            String refMtns = header.getDataList().get(0).getReferenceMtn();
            long diff;

            for (BcqData data : header.getDataList()) {
                if(!refMtns.equals(data.getReferenceMtn())) {
                    previousDate = null;
                    refMtns = data.getReferenceMtn();
                }
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

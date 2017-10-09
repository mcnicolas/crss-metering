package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqProhibitedPair;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.utils.DateTimeUtils;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.ProhibitedValidationHelper;
import com.pemc.crss.metering.validator.bcq.validation.ProhibitedValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.BcqValidationError.CONTAINS_PROHIBITED_PAIRS;
import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_RESUBMISSION_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.helper.BcqValidationHelperUtils.getFormattedSellingMtnAndBillingIdPair;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProhibitedValidationHelperImpl implements ProhibitedValidationHelper {

    private final BcqService bcqService;

    @Override
    public ProhibitedValidation noProhibitedPairs() {
        ProhibitedValidation validation = new ProhibitedValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<BcqHeader> headersWithProhibitedPairs = new ArrayList<>();
            headerList.forEach(header -> {
                List<BcqProhibitedPair> enabledProhibitedPairs = bcqService.findAllEnabledProhibitedPairs().stream()
                        .filter(prohibitedPair -> prohibitedPair.getSellingMtn().toUpperCase().equals(header.getSellingMtn().toUpperCase())
                                && prohibitedPair.getBillingId().toUpperCase().equals(header.getBillingId().toUpperCase()))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(enabledProhibitedPairs)) {
                    boolean isValid = validateTradingDate(enabledProhibitedPairs, header.getTradingDate());
                    if (!isValid) {
                        headersWithProhibitedPairs.add(header);
                    }
                }
            });

            if (!headersWithProhibitedPairs.isEmpty()) {
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(CONTAINS_PROHIBITED_PAIRS,
                        singletonList(getFormattedSellingMtnAndBillingIdPair(headersWithProhibitedPairs)));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private boolean validateTradingDate(List<BcqProhibitedPair> prohibitedPairs, Date tradingDate) {
        Boolean result = true;
        Instant instant = Instant.ofEpochMilli(tradingDate.getTime());
        LocalDateTime newTradingDate = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        for (BcqProhibitedPair pair : prohibitedPairs) {
            if (pair.getEffectiveEndDate() != null) {
                if (DateTimeUtils.isBetweenInclusive(newTradingDate, pair.getEffectiveStartDate(), pair.getEffectiveEndDate())) {
                    result = false;
                }
            } else {
                if (newTradingDate.isAfter(pair.getEffectiveStartDate())) {
                    result = false;
                }
            }
        }
        return result;
    }
}



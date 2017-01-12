package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.helper.HeaderListValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static java.lang.Integer.parseInt;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeaderListValidator {

    private final CacheManager cacheManager;
    private final HeaderListValidationHelper validationHelper;

    public BcqValidationResult validate(List<BcqHeader> headerList, BcqInterval interval) {
        log.info("Start validation of header list");
        BcqValidationResult result = validationHelper
                .validHeaderList(getTradingDateConfig(), interval).test(headerList);
        log.info("Finish validation of header list, Result: {}", result);
        return result;
    }

    public BcqValidationResult validateForSettlement(List<BcqHeader> headerList, BcqInterval interval,
                                                     Date tradingDate) {

        log.info("Start settlement validation of header list");
        BcqValidationResult result = validationHelper
                .validHeaderList(tradingDate, interval).test(headerList);
        log.info("Finish settlement validation of header list, Result: {}", result);
        return result;
    }

    private int getTradingDateConfig() {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper valueWrapper = configCache.get("BCQ_ALLOWABLE_TRADING_DATE");
        return valueWrapper == null ? 1 : parseInt(valueWrapper.get().toString());
    }
}

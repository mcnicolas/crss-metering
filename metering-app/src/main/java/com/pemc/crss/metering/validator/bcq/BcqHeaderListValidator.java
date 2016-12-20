package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.validator.bcq.helper.BcqHeaderListValidationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Integer.parseInt;

@Slf4j
@Component
public class BcqHeaderListValidator {

    private CacheManager cacheManager;
    private BcqHeaderListValidationHelper validationHelper;

    @Autowired
    public BcqHeaderListValidator(CacheManager cacheManager, BcqHeaderListValidationHelper validationHelper) {
        this.cacheManager = cacheManager;
        this.validationHelper = validationHelper;
    }

    public BcqValidationResult validate(List<BcqHeader> headerList, BcqInterval interval) {
        log.info("Start validation of Header List");
        BcqValidationResult result = validationHelper.validHeaderList(getDeclarationDeadlineConfig(), interval).test(headerList);
        log.info("Finish validation of Header List, Result: {}", result);
        return result;
    }

    private int getDeclarationDeadlineConfig() {
        Cache configCache = cacheManager.getCache("config");
        Cache.ValueWrapper intervalWrapper = configCache.get("BCQ_DECLARATION_DEADLINE");
        return intervalWrapper == null ? 1
                : parseInt(configCache.get("BCQ_DECLARATION_DEADLINE").get().toString());
    }
}

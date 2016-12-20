package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.validator.bcq.helper.CsvValidationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Integer.parseInt;

@Slf4j
@Component
public class CsvValidator {

    private CacheManager cacheManager;
    private CsvValidationHelper validationHelper;

    @Autowired
    public CsvValidator(CacheManager cacheManager, CsvValidationHelper validationHelper) {
        this.cacheManager = cacheManager;
        this.validationHelper = validationHelper;
    }

    public BcqValidationResult validate(List<List<String>> csv) {
        log.info("Start validation of CSV file");
        BcqValidationResult result = validationHelper.validCsv(getIntervalConfig()).test(csv);
        log.info("Finish validation of CSV file, Result: {}", result);
        return result;
    }

    private int getIntervalConfig() {
        Cache configCache = cacheManager.getCache("config");
        Cache.ValueWrapper intervalWrapper = configCache.get("BCQ_INTERVAL");
        return intervalWrapper == null ? 15
                : parseInt(configCache.get("BCQ_INTERVAL").get().toString());
    }

}

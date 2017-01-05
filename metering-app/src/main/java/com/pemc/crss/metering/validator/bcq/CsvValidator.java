package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.validator.bcq.helper.CsvValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Integer.parseInt;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CsvValidator {

    private final CacheManager cacheManager;
    private final CsvValidationHelper validationHelper;

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

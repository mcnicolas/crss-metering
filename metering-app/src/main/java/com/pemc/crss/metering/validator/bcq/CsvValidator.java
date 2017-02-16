package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.validator.bcq.helper.CsvValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CsvValidator {

    private final CacheConfigService configService;
    private final CsvValidationHelper validationHelper;

    public BcqValidationResult validate(List<List<String>> csv) {
        log.info("Start validation of CSV file");
        int intervalConfig = configService.getIntegerValueForKey("BCQ_INTERVAL", 15);
        BcqValidationResult result = validationHelper.validCsv(intervalConfig).test(csv);
        log.info("Finish validation of CSV file, Result: {}", result);
        return result;
    }

}

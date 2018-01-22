package com.pemc.crss.metering.validator.bcq.impl;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.CsvValidator;
import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator;
import com.pemc.crss.metering.validator.bcq.helper.CsvValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CsvValidatorImpl implements CsvValidator {

    private final CacheConfigService configService;
    private final CsvValidationHelper validationHelper;

    @Override
    @SuppressWarnings("unchecked")
    public BcqValidationResult<List<BcqHeader>> validate(List<List<String>> csv) {
        log.info("Start validation of CSV file");
        int intervalConfig = configService.getIntegerValueForKey("BCQ_INTERVAL", 15);
        boolean genToGen = BooleanUtils.toBoolean(configService.getValueForKey("GEN_TO_GEN"));
        boolean loadToLoad = BooleanUtils.toBoolean(configService.getValueForKey("LOAD_TO_LOAD"));
        boolean loadToGen = BooleanUtils.toBoolean(configService.getValueForKey("LOAD_TO_GEN"));
        BcqValidationResult<List<BcqHeader>> result = validationHelper.validCsv(intervalConfig).test(csv);
        if (result.getStatus() == ACCEPTED) {
            result.setProcessedObject(new BcqPopulator().populate(csv));
        }
        log.info("Finish validation of CSV file, Result: {}", result);
        return result;
    }

}

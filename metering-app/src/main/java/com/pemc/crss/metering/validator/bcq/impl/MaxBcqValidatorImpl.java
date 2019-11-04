package com.pemc.crss.metering.validator.bcq.impl;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.MaxBcqValidator;
import com.pemc.crss.metering.validator.bcq.helper.MaxBcqValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MaxBcqValidatorImpl implements MaxBcqValidator {

    private final CacheConfigService configService;
    private final MaxBcqValidationHelper validationHelper;

    @Override
    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList) {
        log.info("Start validation of bcq values against max bcq");
        int maxBcqConfig = configService.getIntegerValueForKey("BCQ_UPLOAD_MAX_BCQ", 1000);
        int intervalConfig = configService.getIntegerValueForKey("BCQ_INTERVAL", 15);
        int totalCount = intervalConfig == 15 ? 4 : 12;

        BcqValidationResult<List<BcqHeader>> result = validationHelper.validMaxBcq(maxBcqConfig, totalCount).test(headerList);
        result.setProcessedObject(headerList);
        log.info("Finish validation of bcq values against max bcq config, Result: {}", result);
        return result;    }
}

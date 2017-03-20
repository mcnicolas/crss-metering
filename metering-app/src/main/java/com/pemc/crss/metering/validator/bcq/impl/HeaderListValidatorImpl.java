package com.pemc.crss.metering.validator.bcq.impl;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.HeaderListValidator;
import com.pemc.crss.metering.validator.bcq.helper.HeaderListValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeaderListValidatorImpl implements HeaderListValidator {

    private final CacheConfigService configService;
    private final HeaderListValidationHelper validationHelper;

    @SuppressWarnings("unchecked")
    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList) {
        log.info("Start validation of header list");
        int tradingDateConfig = configService.getIntegerValueForKey("BCQ_ALLOWABLE_TRADING_DATE", 1);
        BcqValidationResult<List<BcqHeader>> result = validationHelper.validHeaderList(tradingDateConfig).test(headerList);
        result.setProcessedObject(headerList);
        log.info("Finish validation of header list, Result: {}", result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public BcqValidationResult<List<BcqHeader>> validateForSettlement(List<BcqHeader> headerList, Date tradingDate) {
        log.info("Start settlement validation of header list");
        BcqValidationResult<List<BcqHeader>> result = validationHelper.validHeaderList(tradingDate).test(headerList);
        result.setProcessedObject(headerList);
        log.info("Finish settlement validation of header list, Result: {}", result);
        return result;
    }

}

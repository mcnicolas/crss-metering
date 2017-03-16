package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
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
public class HeaderListValidator {

    private final CacheConfigService configService;
    private final HeaderListValidationHelper validationHelper;

    public BcqValidationResult validate(List<BcqHeader> headerList) {
        log.info("Start validation of header list");
        int tradingDateConfig = configService.getIntegerValueForKey("BCQ_ALLOWABLE_TRADING_DATE", 1);
        BcqValidationResult result = validationHelper.validHeaderList(tradingDateConfig).test(headerList);
        log.info("Finish validation of header list, Result: {}", result);
        return result;
    }

    public BcqValidationResult validateForSettlement(List<BcqHeader> headerList, Date tradingDate) {
        log.info("Start settlement validation of header list");
        BcqValidationResult result = validationHelper.validHeaderList(tradingDate).test(headerList);
        log.info("Finish settlement validation of header list, Result: {}", result);
        return result;
    }
}

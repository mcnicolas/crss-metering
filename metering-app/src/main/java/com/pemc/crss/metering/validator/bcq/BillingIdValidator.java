package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BillingIdShortNamePair;
import com.pemc.crss.metering.validator.bcq.helper.BillingIdValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BillingIdValidator {

    private final BillingIdValidationHelper validationHelper;

    public BcqValidationResult validate(List<BillingIdShortNamePair> billingIdShortNamePairs, Date tradingDate) {
        log.info("Start validation of billing id");
        BcqValidationResult result = validationHelper.validBillingIds(tradingDate).test(billingIdShortNamePairs);
        log.info("Finish validation of CSV file, Result: {}", result);
        return result;
    }

}

package com.pemc.crss.metering.validator.bcq.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.OverrideValidator;
import com.pemc.crss.metering.validator.bcq.helper.OverrideValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OverrideValidatorImpl implements OverrideValidator {

    private final OverrideValidationHelper validationHelper;

    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList, String sellingParticipant) {
        log.debug("Start override validation");
        Date tradingDate = headerList.get(0).getTradingDate();
        BcqValidationResult<List<BcqHeader>> result = validationHelper.validOverride(sellingParticipant, tradingDate)
                .test(headerList);

        result.setProcessedObject(headerList);
        log.debug("Finish override validation, Result: {}", result);
        return result;
    }

}

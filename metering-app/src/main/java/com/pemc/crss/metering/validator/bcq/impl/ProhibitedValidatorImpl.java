package com.pemc.crss.metering.validator.bcq.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.ProhibitedValidator;
import com.pemc.crss.metering.validator.bcq.helper.ProhibitedValidationHelper;
import com.pemc.crss.metering.validator.bcq.helper.impl.ProhibitedValidationHelperImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProhibitedValidatorImpl implements ProhibitedValidator {

    private final ProhibitedValidationHelper validationHelper;

    @Override
    @SuppressWarnings("unchecked")
    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList) {
        log.debug("Start validation of prohibited");
        BcqValidationResult<List<BcqHeader>> result = validationHelper.noProhibitedPairs().test(headerList);
        result.setProcessedObject(headerList);
        log.debug("Finish validation of prohibited, Result: {}", result);
        return result;
    }

}

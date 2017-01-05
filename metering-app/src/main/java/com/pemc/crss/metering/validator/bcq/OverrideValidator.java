package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
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
public class OverrideValidator {

    private final OverrideValidationHelper validationHelper;

    public BcqValidationResult validate(List<BcqHeader> headerList, String sellingParticipant, Date tradingDate) {
        log.debug("Start override validation");
        BcqValidationResult result = validationHelper.validOverride(sellingParticipant, tradingDate).test(headerList);
        log.debug("Finish override validation, Result: {}", result);
        return result;
    }

}

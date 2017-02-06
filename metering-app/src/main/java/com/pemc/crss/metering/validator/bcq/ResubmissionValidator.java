package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.helper.ResubmissionValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResubmissionValidator {

    private final ResubmissionValidationHelper validationHelper;

    public BcqValidationResult validate(List<BcqHeader> headerList, String sellingParticipant, Date tradingDate) {
        log.debug("Start validation of resubmission");
        BcqValidationResult result = validationHelper.validResubmission(sellingParticipant, tradingDate).test(headerList);
        log.debug("Finish validation of resubmission, Result: {}", result);
        return result;
    }

}

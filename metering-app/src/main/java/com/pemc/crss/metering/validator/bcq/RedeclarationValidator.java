package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.helper.RedeclarationValidationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class RedeclarationValidator {

    private final RedeclarationValidationHelper validationHelper;

    @Autowired
    public RedeclarationValidator(RedeclarationValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public BcqValidationResult validate(List<BcqHeader> headerList, String sellingParticipant, Date tradingDate) {
        log.debug("Start validation of Redeclaration");
        BcqValidationResult result = validationHelper.validRedeclaration(sellingParticipant, tradingDate).test(headerList);
        log.debug("Finish validation of Redeclaration, Result: {}", result);
        return result;
    }

}

package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.helper.SpecialEventValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecialEventValidator {

    private final SpecialEventValidationHelper validationHelper;

    @SuppressWarnings("unchecked")
    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList, String sellingParticipant) {
        log.debug("Start validation for special event upload");
        BcqValidationResult<List<BcqHeader>> result = validationHelper.validSpecialEventUpload(sellingParticipant)
                .test(headerList);
        result.setProcessedObject(headerList);
        log.debug("Finish validation for special event upload, Result: {}", result);
        return result;
    }

}

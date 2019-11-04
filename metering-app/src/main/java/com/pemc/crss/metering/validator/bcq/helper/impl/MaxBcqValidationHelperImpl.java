package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.MaxBcqValidationHelper;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.INVALID_BCQ_VALUE;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;

@Component
public class MaxBcqValidationHelperImpl implements MaxBcqValidationHelper {

    @Override
    public Validation<List<BcqHeader>> validMaxBcq(Integer maxBcqConfig, Integer divisor) {
        HeaderListValidation validation = new HeaderListValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            long invalidBcqCount = headerList.get(0).getDataList().stream()
                    .filter(b -> b.getBcq().divide(valueOf(divisor), 9, ROUND_HALF_UP).compareTo(valueOf(maxBcqConfig)) > 0).count();
            if (invalidBcqCount > 0) {
                validation.setErrorMessage(new BcqValidationErrorMessage(INVALID_BCQ_VALUE));
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

}

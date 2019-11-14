package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.MaxBcqValidationHelper;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqInterval.HOURLY;
import static com.pemc.crss.metering.constants.BcqValidationError.INVALID_BCQ_VALUE;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;

@Component
public class MaxBcqValidationHelperImpl implements MaxBcqValidationHelper {

    @Override
    public Validation<List<BcqHeader>> validMaxBcq(int maxBcqConfig, int intervalConfig) {
        HeaderListValidation validation = new HeaderListValidation();
        int totalCount = intervalConfig == 15 ? 4 : 12;
        Predicate<List<BcqHeader>> predicate = headerList -> {
            long invalidBcqCount = headerList.get(0).getDataList().stream().filter(bcqData -> {
                BigDecimal value;
                if (HOURLY.equals(headerList.get(0).getInterval())) {
                    value = bcqData.getBcq().divide(valueOf(totalCount), 9, ROUND_HALF_UP);
                } else {
                    value = bcqData.getBcq();
                }
                return value.compareTo(valueOf(maxBcqConfig)) > 0;
            }).count();
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

package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.validation.Validation;

import java.util.List;

public interface MaxBcqValidationHelper {

    Validation<List<BcqHeader>> validMaxBcq(Integer maxBcqConfig, Integer divisor);

}

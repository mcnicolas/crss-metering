package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.validation.Validation;

import java.util.List;

public interface ProhibitedValidationHelper {

    Validation<List<BcqHeader>> noProhibitedPairs();

}

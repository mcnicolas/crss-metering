package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;

import java.util.List;

public interface CsvValidator {

    BcqValidationResult<List<BcqHeader>> validate(List<List<String>> csv);

}

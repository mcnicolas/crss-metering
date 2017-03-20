package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.validator.bcq.validation.Validation;

import java.util.Date;
import java.util.List;

public interface HeaderListValidationHelper {

    Validation<List<BcqHeader>> validHeaderList(int declarationDateConfig);
    Validation<List<BcqHeader>> validHeaderList(Date tradingDate);

}

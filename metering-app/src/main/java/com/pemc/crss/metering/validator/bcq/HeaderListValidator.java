package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;

import java.util.Date;
import java.util.List;

public interface HeaderListValidator {

    BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList);
    BcqValidationResult<List<BcqHeader>> validateForSettlement(List<BcqHeader> headerList, Date tradingDate);

}

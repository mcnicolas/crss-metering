package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;

import java.util.List;

public interface CrssSideValidator {

    BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList, ParticipantSellerDetails sellerDetails);
    BcqValidationResult<List<BcqHeader>> validateBySettlement(List<BcqHeader> headerList,
                                                              ParticipantSellerDetails sellerDetails);

}

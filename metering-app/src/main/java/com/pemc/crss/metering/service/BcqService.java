package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dto.BcqDeclaration;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.List;

public interface BcqService {

    void saveBcqDetails(BcqUploadFile file, List<BcqDeclaration> bcqDeclarationList,
                        List<Long> buyerIds, Long sellerId);

}

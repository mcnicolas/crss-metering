package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.BcqDeclaration;
import com.pemc.crss.metering.dto.BcqDeclarationDisplay;
import com.pemc.crss.metering.dto.BcqUploadFile;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BcqService {

    void saveBcqDetails(BcqUploadFile file, List<BcqDeclaration> bcqDeclarationList,
                        List<Long> buyerIds, Long sellerId);

    Page<BcqDeclarationDisplay> findAll(PageableRequest pageableRequest);

}

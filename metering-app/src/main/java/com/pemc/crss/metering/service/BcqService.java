package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BcqService {

    void saveBcqDetails(BcqUploadFile file, List<BcqDeclaration> bcqDeclarationList,
                        List<Long> buyerIds, Long sellerId);

    Page<BcqDeclarationDisplay> findAllBcqDeclarations(PageableRequest pageableRequest);

    BcqDeclarationDisplay findBcqDeclaration(long headerId);

    List<BcqData> findAllBcqData(long headerId);

}

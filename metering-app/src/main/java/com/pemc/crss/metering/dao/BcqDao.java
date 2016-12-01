package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BcqDao {

    long saveBcqUploadFile(String transactionID, BcqUploadFile uploadFile);

    List<Long> saveBcqDeclaration(long fileID, List<BcqDeclaration> bcqDeclarationList);

    Page<BcqDeclarationDisplay> findAllBcqDeclarations(PageableRequest pageableRequest);

    BcqDeclarationDisplay findBcqDeclaration(long headerId);

    List<BcqData> findAllBcqData(long headerId);

}

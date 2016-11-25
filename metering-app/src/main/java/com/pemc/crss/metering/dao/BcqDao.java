package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.BcqDeclaration;
import com.pemc.crss.metering.dto.BcqDeclarationDisplay;
import com.pemc.crss.metering.dto.BcqUploadFile;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BcqDao {

    long saveBcqUploadFile(String transactionID, BcqUploadFile uploadFile);

    void saveBcqDeclaration(long fileID, List<BcqDeclaration> bcqDeclarationList);

    Page<BcqDeclarationDisplay> findAll(PageableRequest pageableRequest);

}

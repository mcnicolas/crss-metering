package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BcqDao {

    long saveUploadFile(String transactionID, BcqUploadFile uploadFile);

    List<Long> saveBcq(long fileID, List<BcqHeader> headerList);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    BcqHeader findHeader(long headerId);

    List<BcqData> findAllBcqData(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

    boolean headerExists(BcqHeader header);

}

package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BcqDao {

    long saveUploadFile(BcqUploadFile uploadFile);

    List<Long> saveBcq(long fileID, List<BcqHeader> headerList);

    List<BcqHeader> findAllHeaders(Map<String, String> params);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    BcqHeader findHeader(long headerId);

    List<BcqData> findAllBcqData(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

    boolean headerExists(BcqHeader header);

}

package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BcqDao2 {

    long saveUploadFile(BcqUploadFile uploadFile);

    List<BcqHeader> saveHeaderList(List<BcqHeader> headerList);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    List<BcqHeader> findAllHeaders(Map<String, String> params);

    BcqHeader findHeader(long headerId);

    List<BcqData> findDataByHeaderId(long headerId);

    void updateHeaderStatus(long headerId, BcqStatus status);

}

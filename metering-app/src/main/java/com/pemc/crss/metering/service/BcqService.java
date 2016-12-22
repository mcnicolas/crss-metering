package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BcqService {

    long saveUploadFile(BcqUploadFile uploadFile);

    void save(BcqDetails details);

    List<BcqHeader> findAllHeaders(Map<String, String> params);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    BcqHeader findHeader(long headerId);

    List<BcqData> findAllData(long headerId);

    void updateHeaderStatus(long headerId, BcqUpdateStatusDetails updateStatusDetails);

    boolean headerExists(BcqHeader header);

    boolean isHeaderInList(BcqHeader headerToFind, List<BcqHeader> headerList);

}

package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUpdateStatusDetails;
import com.pemc.crss.metering.dto.BcqUploadFile;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BcqService {

    void saveBcq(BcqUploadFile file, List<BcqHeader> headerList,
                 List<Long> buyerIds, Long sellerId);

    Page<BcqHeader> findAllHeaders(PageableRequest pageableRequest);

    BcqHeader findHeader(long headerId);

    List<BcqData> findAllData(long headerId);

    void updateHeaderStatus(long headerId, BcqUpdateStatusDetails updateStatusDetails);

}

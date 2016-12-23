package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.List;
import java.util.Map;

public interface BcqDao2 {

    long saveUploadFile(BcqUploadFile uploadFile);

    List<BcqHeader> saveHeaderList(List<BcqHeader> headerList);

    List<BcqHeader> findAllHeaders(Map<String, String> params);

}

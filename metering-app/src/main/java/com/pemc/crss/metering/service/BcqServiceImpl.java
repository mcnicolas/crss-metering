package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BcqServiceImpl implements BcqService {

    @NonNull
    private final BcqDao bcqDao;

    @Override
    public long saveBcqUploadFile(String transactionID, BcqUploadFile bcqUploadFile) {
        return bcqDao.saveBcqUploadFile(transactionID, bcqUploadFile);
    }

    @Override
    public void saveBcqData(long fileID, Map<BcqHeader, Set<BcqData>> headerDataMap) {
        bcqDao.saveBcqData(fileID, headerDataMap);
    }
}

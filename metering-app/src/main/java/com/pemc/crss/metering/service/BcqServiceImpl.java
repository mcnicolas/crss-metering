package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dao.BcqDao;
import com.pemc.crss.metering.dto.BcqHeaderDataPair;
import com.pemc.crss.metering.dto.BcqUploadFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

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
    public void saveBcqData(long fileID, List<BcqHeaderDataPair> headerDataPairList) {
        bcqDao.saveBcqData(fileID, headerDataPairList);
    }
}

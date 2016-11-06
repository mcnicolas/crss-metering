package com.pemc.crss.metering.service;

import com.pemc.crss.metering.dao.BCQDao;
import com.pemc.crss.metering.dto.BCQUploadFile;
import com.pemc.crss.metering.parser.bcq.BCQReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BCQServiceImpl implements BCQService {

    @NonNull
    private final BCQDao bcqDao;

    @NonNull
    private final BCQReader reader;

    @Override
    public long saveBCQUploadFile(String transactionID, BCQUploadFile bcqUploadFile) {
        return bcqDao.saveBCQUploadFile(transactionID, bcqUploadFile);
    }

    @Override
    public void saveBCQData(long fileID, byte[] fileContent) throws IOException {
        bcqDao.saveBCQData(fileID, reader.readData(new ByteArrayInputStream(fileContent)));
    }
}

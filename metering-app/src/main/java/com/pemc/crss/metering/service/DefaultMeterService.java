package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterUploadFile;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.parser.MDEFReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import static com.pemc.crss.metering.constants.FileType.MDEF;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

@Service
public class DefaultMeterService implements MeterService {

    @Autowired
    private MeteringDao meteringDao;

    @Autowired
    private MDEFReader mdefReader;

    @Override
    @Transactional
    public void saveMeterData(Collection<MultipartFile> multipartFiles, UploadType uploadType) throws IOException {
        MeterUploadHeader meterUploadHeader = new MeterUploadHeader();

        // TODO: Retrieve MSP ID from Registration Service
        meterUploadHeader.setMspID(1L);

        // TODO: Retrieve username from Admin Service
        meterUploadHeader.setUploadedBy("xx");

        meterUploadHeader.setCategory(uploadType.toString());
        meterUploadHeader.setUploadedDateTime(new Date());
        meterUploadHeader.setVersion(1);

        long transactionID = meteringDao.saveMeterUploadHeader(meterUploadHeader);

        for (MultipartFile file : multipartFiles) {
            MeterUploadFile meterUploadFile = new MeterUploadFile();

            meterUploadFile.setTransactionID(transactionID);

            String filename = file.getOriginalFilename();
            meterUploadFile.setFileName(filename);

            // TODO: Dirty code consider revising
            String fileExtension = getExtension(filename);
            if (equalsIgnoreCase(fileExtension, "MDE") || equalsIgnoreCase(fileExtension, "MDEF")) {
                meterUploadFile.setFileType(MDEF);
            } else if (equalsIgnoreCase(fileExtension, "XLS") || equalsIgnoreCase(fileExtension, "XLSX")) {
                meterUploadFile.setFileType(XLS);
            }

            meterUploadFile.setFileSize(file.getSize());

            // TODO: Update when validation routine will be implemented
            meterUploadFile.setStatus(ACCEPTED);

            long fileID = meteringDao.saveMeterUploadFile(transactionID, meterUploadFile);

            // Parse
            MeterData meterData = mdefReader.readMDEF(file.getInputStream());
            meteringDao.saveMeterUploadMDEF(fileID, meterData);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return null;
        }

        int index = filename.lastIndexOf('.');

        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

}

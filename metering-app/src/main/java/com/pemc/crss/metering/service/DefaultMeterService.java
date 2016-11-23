package com.pemc.crss.metering.service;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterData2;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.MeterUploadFile;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.parser.QuantityReader;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityReaderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.FileType.MDEF;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

@Slf4j
@Service
public class DefaultMeterService implements MeterService {

    private final MeteringDao meteringDao;
    private final MeterQuantityReaderFactory readerFactory;

    @Autowired
    public DefaultMeterService(MeteringDao meteringDao, MeterQuantityReaderFactory readerFactory) {
        this.meteringDao = meteringDao;
        this.readerFactory = readerFactory;
    }

    @Override
    @Transactional
    public long saveHeader(String transactionID, long mspID, int fileCount, String category, String username) {
        return meteringDao.saveHeader(transactionID, mspID, fileCount, category, username);
    }

    @Override
    @Transactional
    public void saveTrailer(String transactionID) {
        meteringDao.saveTrailer(transactionID);
    }

    @Override
    @Transactional
    public long saveFileManifest(long headerID, String transactionID, String fileName, String fileType, long fileSize,
                                 String checksum) {
        return meteringDao.saveFileManifest(headerID, transactionID, fileName, fileType, fileSize, checksum);
    }

    @Override
    @Transactional
    public void saveMeterData(long fileID, String fileType, byte[] fileContent, String category) {
        try {
            // TODO: Validate

            QuantityReader<MeterData2> reader = readerFactory.getMeterQuantityReader(fileType);
            List<MeterData2> meterData = reader.readData(new ByteArrayInputStream(fileContent));

            meteringDao.saveMeterData(fileID, meterData, category);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterDataDisplay> getMeterData(PageableRequest pageableRequest) {
        int totalRecords = meteringDao.getTotalRecords(pageableRequest);
        List<MeterDataDisplay> meterDataList = meteringDao.findAll(pageableRequest);

        return new PageImpl<>(
                meterDataList,
                pageableRequest.getPageable(),
                totalRecords);
    }

    @Deprecated // Change in impl
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
//            MeterData meterData = mdefReader.readMDEF(file.getInputStream());
//            meteringDao.saveMeterUploadMDEF(fileID, meterData);
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

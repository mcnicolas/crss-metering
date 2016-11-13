package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.exception.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/bcq")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BcqResource {

    @NonNull
    private BcqReader bcqReader;

    @NonNull
    private BcqService bcqService;

    @PostMapping("/upload")
    public BcqDetails uploadData(@RequestParam("file") MultipartFile file)
            throws IOException, ValidationException {

        Map<BcqHeaderInfo, Set<BcqDataInfo>> headerDataMap = new HashMap<>();
        BcqUploadFileInfo fileInfo = new BcqUploadFileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());

        bcqReader.readData(file.getInputStream(), new Date()).entrySet().forEach(entry -> {
            BcqHeader header = entry.getKey();
            BcqHeaderInfo headerInfo = new BcqHeaderInfo();

            headerInfo.setSellingMTN(header.getSellingMTN());
            headerInfo.setBuyingParticipant(header.getBuyingParticipant());
            headerInfo.setSellingParticipantName(null);
            headerInfo.setSellingParticipantShortName(null);
            headerInfo.setStatus(null);

            Set<BcqData> dataSet = entry.getValue();
            Set<BcqDataInfo> dataInfoSet = new HashSet<>();

            for (BcqData data : dataSet) {
                BcqDataInfo dataInfo = new BcqDataInfo();

                dataInfo.setReferenceMTN(data.getReferenceMTN());
                dataInfo.setStartTime(data.getStartTime());
                dataInfo.setEndTime(data.getEndTime());
                dataInfo.setBcq(data.getBcq());
                dataInfoSet.add(dataInfo);
            }

            headerDataMap.put(headerInfo, dataInfoSet);
        });

        return new BcqDetails(fileInfo, headerDataMap);
    }

    @PostMapping("/save")
    public Boolean saveData(@RequestBody BcqDetails details) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(details.getFileInfo().getFileName());
        uploadFile.setFileSize(details.getFileInfo().getFileSize());
        uploadFile.setSubmittedDate(new Date());

        Map<BcqHeader, Set<BcqData>> headerDataMap = new HashMap<>();

        details.getHeaderDataMap().entrySet().forEach(entry -> {
            BcqHeaderInfo headerInfo = entry.getKey();
            BcqHeader header = new BcqHeader();

            header.setSellingMTN(headerInfo.getSellingMTN());
            header.setBuyingParticipant(headerInfo.getBuyingParticipant());
            header.setSellingParticipantName(headerInfo.getSellingParticipantName());
            header.setSellingParticipantShortName(headerInfo.getSellingParticipantShortName());
            header.setStatus(headerInfo.getStatus());

            Set<BcqDataInfo> dataInfoSet = entry.getValue();
            Set<BcqData> dataSet = new HashSet<>();

            for (BcqDataInfo dataInfo : dataInfoSet) {
                BcqData data = new BcqData();

                data.setReferenceMTN(dataInfo.getReferenceMTN());
                data.setStartTime(dataInfo.getStartTime());
                data.setEndTime(dataInfo.getEndTime());
                data.setBcq(dataInfo.getBcq());
                dataSet.add(data);
            }

            headerDataMap.put(header, dataSet);
        });

        String transactionId = UUID.randomUUID().toString();
        long fileId = bcqService.saveBcqUploadFile(transactionId, uploadFile);

        bcqService.saveBcqData(fileId, headerDataMap);

        return true;
    }
}

package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.BCQStatus;
import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.parser.bcq.BCQReader;
import com.pemc.crss.metering.service.BCQService;
import com.pemc.crss.metering.validator.exception.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/bcq")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BCQResource {

    @NonNull
    private BCQReader bcqReader;

    @NonNull
    private BCQService bcqService;

    @PostMapping("/upload")
    public BCQDetails uploadData(@RequestParam("file") MultipartFile file) throws IOException, ValidationException {
        BCQUploadFileInfo fileInfo = new BCQUploadFileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());

        List<BCQDataInfo> dataInfoList = new ArrayList<>();

        bcqReader.readData(file.getInputStream()).forEach(data -> {
            BCQDataInfo dataInfo = new BCQDataInfo();
            dataInfo.setSellingMTN(data.getSellingMTN());
            dataInfo.setBuyingParticipant(data.getBuyingParticipant());
            dataInfo.setReferenceMTN(data.getReferenceMTN());
            dataInfo.setStartTime(data.getStartTime());
            dataInfo.setEndTime(data.getEndTime());
            dataInfo.setBcq(data.getBcq());
            dataInfoList.add(dataInfo);
        });

        return new BCQDetails(fileInfo, dataInfoList);
    }

    @PostMapping("/save")
    public BCQDetails saveData(@RequestBody BCQDetails details) {
        BCQUploadFile uploadFile = new BCQUploadFile();
        uploadFile.setFileName(details.getFileInfo().getFileName());
        uploadFile.setFileSize(details.getFileInfo().getFileSize());
        uploadFile.setStatus(BCQStatus.NOT_CONFIRMED);

        List<BCQData> dataList = new ArrayList<>();

        details.getDataInfoList().forEach(dataInfo -> {
            BCQData data = new BCQData();
            data.setSellingMTN(dataInfo.getSellingMTN());
            data.setBuyingParticipant(dataInfo.getBuyingParticipant());
            data.setReferenceMTN(dataInfo.getReferenceMTN());
            data.setStartTime(dataInfo.getStartTime());
            data.setEndTime(dataInfo.getEndTime());
            data.setBcq(dataInfo.getBcq());
            dataList.add(data);
        });

        String transactionId = UUID.randomUUID().toString();
        long fileId = bcqService.saveBCQUploadFile(transactionId, uploadFile);

        bcqService.saveBCQData(fileId, dataList);

        return details;
    }
}

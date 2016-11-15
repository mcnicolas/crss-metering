package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.BcqStatus;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        List<BcqHeaderDataInfoPair> headerDataInfoPairList = new ArrayList<>();
        BcqUploadFileInfo fileInfo = new BcqUploadFileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());

        bcqReader.readData(file.getInputStream(), null).forEach(headerDataPair -> {
            BcqHeader header = headerDataPair.getHeader();
            BcqHeaderInfo headerInfo = new BcqHeaderInfo();

            headerInfo.setSellingMTN(header.getSellingMTN());
            headerInfo.setBuyingParticipant(header.getBuyingParticipant());
            headerInfo.setSellingParticipantName(null);
            headerInfo.setSellingParticipantShortName(null);
            headerInfo.setStatus(null);
            headerInfo.setDeclarationDate(header.getDeclarationDate());

            List<BcqData> dataList = headerDataPair.getDataList();
            List<BcqDataInfo> dataInfoList = new ArrayList<>();

            for (BcqData data : dataList) {
                BcqDataInfo dataInfo = new BcqDataInfo();

                dataInfo.setReferenceMTN(data.getReferenceMTN());
                dataInfo.setStartTime(data.getStartTime());
                dataInfo.setEndTime(data.getEndTime());
                dataInfo.setBcq(data.getBcq());
                dataInfoList.add(dataInfo);
            }

            headerDataInfoPairList.add(new BcqHeaderDataInfoPair(headerInfo, dataInfoList));
        });

        return new BcqDetails(null, fileInfo, headerDataInfoPairList, null);
    }

    @PostMapping("/save")
    public void saveData(@RequestBody BcqDetails details) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(details.getFileInfo().getFileName());
        uploadFile.setFileSize(details.getFileInfo().getFileSize());
        uploadFile.setSubmittedDate(new Date());

        List<BcqHeaderDataPair> headerDataPairList = new ArrayList<>();

        details.getHeaderDataInfoPairList().forEach(headerDataInfoPair -> {
            BcqHeaderInfo headerInfo = headerDataInfoPair.getHeaderInfo();
            BcqHeader header = new BcqHeader();

            header.setSellingMTN(headerInfo.getSellingMTN());
            header.setBuyingParticipant(headerInfo.getBuyingParticipant());
            header.setSellingParticipantName(headerInfo.getSellingParticipantName());
            header.setSellingParticipantShortName(headerInfo.getSellingParticipantShortName());
            header.setStatus(BcqStatus.fromString(headerInfo.getStatus()));
            header.setDeclarationDate(headerInfo.getDeclarationDate());

            List<BcqDataInfo> dataInfoList = headerDataInfoPair.getDataInfoList();
            List<BcqData> dataList = new ArrayList<>();

            for (BcqDataInfo dataInfo : dataInfoList) {
                BcqData data = new BcqData();

                data.setReferenceMTN(dataInfo.getReferenceMTN());
                data.setStartTime(dataInfo.getStartTime());
                data.setEndTime(dataInfo.getEndTime());
                data.setBcq(dataInfo.getBcq());
                dataList.add(data);
            }

            headerDataPairList.add(new BcqHeaderDataPair(header, dataList));
        });

        bcqService.saveBcqDetails(uploadFile, headerDataPairList, details.getBuyerIds(), details.getSellerId());
    }
}

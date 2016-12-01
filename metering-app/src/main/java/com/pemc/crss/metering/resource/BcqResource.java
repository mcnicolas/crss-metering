package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.event.BcqUploadEvent;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqNotificationRecipient.BILLING;
import static com.pemc.crss.metering.constants.BcqNotificationRecipient.SELLER;
import static com.pemc.crss.metering.constants.BcqNotificationType.VALIDATION;
import static com.pemc.crss.metering.constants.BcqUploadEventCode.NTF_BCQ_VALIDATION_DEPT;
import static com.pemc.crss.metering.constants.BcqUploadEventCode.NTF_BCQ_VALIDATION_SELLER;

@Slf4j
@RestController
@RequestMapping("/bcq")
public class BcqResource extends BaseListResource<BcqDeclarationDisplay> { //TODO: Use DTO mapper

    private BcqReader bcqReader;
    private BcqService bcqService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public BcqResource(BcqReader bcqReader, BcqService bcqService, ApplicationEventPublisher eventPublisher) {
        this.bcqReader = bcqReader;
        this.bcqService = bcqService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/upload")
    public BcqDetails uploadData(@RequestParam("file") MultipartFile file)
            throws IOException, ValidationException {

        List<BcqDeclarationInfo> bcqDeclarationInfoList = new ArrayList<>();
        BcqUploadFileInfo fileInfo = new BcqUploadFileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());

        bcqReader.readData(file.getInputStream(), null).forEach(bcqDeclaration -> {
            BcqHeader header = bcqDeclaration.getHeader();
            BcqHeaderInfo headerInfo = new BcqHeaderInfo();

            headerInfo.setSellingMtn(header.getSellingMtn());
            headerInfo.setBuyingParticipant(header.getBuyingParticipant());
            headerInfo.setSellingParticipantName(null);
            headerInfo.setSellingParticipantShortName(null);
            headerInfo.setStatus(null);
            headerInfo.setTradingDate(header.getTradingDate());

            List<BcqData> dataList = bcqDeclaration.getDataList();
            List<BcqDataInfo> dataInfoList = new ArrayList<>();

            for (BcqData data : dataList) {
                BcqDataInfo dataInfo = new BcqDataInfo();

                dataInfo.setReferenceMtn(data.getReferenceMtn());
                dataInfo.setStartTime(data.getStartTime());
                dataInfo.setEndTime(data.getEndTime());
                dataInfo.setBcq(data.getBcq().toPlainString());
                dataInfoList.add(dataInfo);
            }

            bcqDeclarationInfoList.add(new BcqDeclarationInfo(headerInfo, dataInfoList));
        });

        return new BcqDetails(null, fileInfo, bcqDeclarationInfoList, null);
    }

    @PostMapping("/save")
    public void saveData(@RequestBody BcqDetails details) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(details.getFileInfo().getFileName());
        uploadFile.setFileSize(details.getFileInfo().getFileSize());
        uploadFile.setSubmittedDate(new Date());

        List<BcqDeclaration> bcqDeclarationList = new ArrayList<>();

        details.getBcqDeclarationInfoList().forEach(declarationInfo -> {
            BcqHeaderInfo headerInfo = declarationInfo.getHeaderInfo();
            BcqHeader header = new BcqHeader();

            header.setSellingMtn(headerInfo.getSellingMtn());
            header.setBuyingParticipant(headerInfo.getBuyingParticipant());
            header.setSellingParticipantName(headerInfo.getSellingParticipantName());
            header.setSellingParticipantShortName(headerInfo.getSellingParticipantShortName());
            header.setStatus(BcqStatus.fromString(headerInfo.getStatus()));
            header.setTradingDate(headerInfo.getTradingDate());

            List<BcqDataInfo> dataInfoList = declarationInfo.getDataInfoList();
            List<BcqData> dataList = new ArrayList<>();

            for (BcqDataInfo dataInfo : dataInfoList) {
                BcqData data = new BcqData();

                data.setReferenceMtn(dataInfo.getReferenceMtn());
                data.setStartTime(dataInfo.getStartTime());
                data.setEndTime(dataInfo.getEndTime());
                data.setBcq(new BigDecimal(dataInfo.getBcq()));
                dataList.add(data);
            }

            bcqDeclarationList.add(new BcqDeclaration(header, dataList));
        });

        bcqService.saveBcqDetails(uploadFile, bcqDeclarationList, details.getBuyerIds(), details.getSellerId());
    }

    @PostMapping("/validation-error")
    public void validationError(@RequestBody Map<String, String> source) {
        String format = "MMM. dd, yyyy hh:mm";
        DateFormat dateFormat = new SimpleDateFormat(format);
        String submittedDate = dateFormat.format(new Date());

        Map<String, Object> payload = new HashMap<>();
        payload.put("sellerId", Long.parseLong(source.get("userId")));
        payload.put("submittedDate", submittedDate);
        payload.put("errorMessage", source.get("errorMessage"));

        BcqUploadEvent eventSeller = new BcqUploadEvent(payload, NTF_BCQ_VALIDATION_SELLER, VALIDATION, SELLER);
        eventPublisher.publishEvent(eventSeller);

        payload.put("sellerName", source.get("participantName"));
        payload.put("sellerShortName", source.get("participantShortName"));

        BcqUploadEvent eventDept = new BcqUploadEvent(payload, NTF_BCQ_VALIDATION_DEPT, VALIDATION, BILLING);
        eventPublisher.publishEvent(eventDept);
    }

    @GetMapping("/declaration/{headerId}")
    public BcqDeclarationDisplay getDeclaration(@PathVariable long headerId) {
        return bcqService.findBcqDeclaration(headerId);
    }

    @GetMapping("/data/{headerId}")
    public List<BcqDataInfo> getData(@PathVariable long headerId) {
        List<BcqDataInfo> dataInfoList = new ArrayList<>();

        for (BcqData data : bcqService.findAllBcqData(headerId)) {
            BcqDataInfo dataInfo = new BcqDataInfo();

            dataInfo.setReferenceMtn(data.getReferenceMtn());
            dataInfo.setEndTime(data.getEndTime());
            dataInfo.setBcq(data.getBcq().toPlainString());
            dataInfoList.add(dataInfo);
        }
        return dataInfoList;
    }

    @Override
    public DataTableResponse<BcqDeclarationDisplay> executeSearch(PageableRequest request) {
        Page<BcqDeclarationDisplay> bcqDeclarationPage = bcqService.findAllBcqDeclarations(request);

        return new DataTableResponse<BcqDeclarationDisplay>()
                .withData(bcqDeclarationPage.getContent())
                .withRecordsTotal(bcqDeclarationPage.getTotalElements());
    }
}

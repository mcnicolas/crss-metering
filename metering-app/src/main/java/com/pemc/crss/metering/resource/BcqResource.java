package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.event.BcqEvent;
import com.pemc.crss.metering.event.BcqValidationDeptEvent;
import com.pemc.crss.metering.event.BcqValidationSellerEvent;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequestMapping("/bcq")
public class BcqResource extends BaseListResource<BcqHeaderDisplay> { //TODO: Use DTO mapper

    private BcqReader bcqReader;
    private BcqService bcqService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public BcqResource(BcqReader bcqReader, BcqService bcqService, ApplicationEventPublisher eventPublisher) {
        this.bcqReader = bcqReader;
        this.bcqService = bcqService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DataTableResponse<BcqHeaderDisplay> executeSearch(PageableRequest request) {
        Page<BcqHeader> headerPage = bcqService.findAllHeaders(request);
        List<BcqHeaderDisplay> headerDisplayList = new ArrayList<>();
        headerPage.getContent().forEach(header -> headerDisplayList.add(new BcqHeaderDisplay(header)));

        return new DataTableResponse<BcqHeaderDisplay>()
                .withData(headerDisplayList)
                .withRecordsTotal(headerPage.getTotalElements());
    }

    @PostMapping(value = "/uploadFile", consumes = MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return "";
    }

    @PostMapping("/upload")
    public BcqDetails uploadData(@RequestParam("file") MultipartFile file)
            throws IOException, ValidationException {

        List<BcqHeaderInfo> headerInfoList = new ArrayList<>();
        BcqUploadFileInfo fileInfo = new BcqUploadFileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());

        List<BcqHeader> headerList = bcqReader.readData(file.getInputStream(), null);

        headerList.forEach(header -> {
            boolean headerExists = bcqService.headerExists(header);
            BcqHeaderInfo headerInfo = new BcqHeaderInfo(header);
            headerInfo.setHeaderExists(headerExists);
            headerInfoList.add(headerInfo);
        });

        return new BcqDetails(null, fileInfo, headerInfoList, null);
    }

    @PostMapping("/save")
    public void saveData(@RequestBody BcqDetails details) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(details.getFileInfo().getFileName());
        uploadFile.setFileSize(details.getFileInfo().getFileSize());
        uploadFile.setSubmittedDate(new Date());

        List<BcqHeader> headerList = new ArrayList<>();
        details.getHeaderInfoList().forEach(headerInfo -> {
            headerList.add(headerInfo.target());
        });

        bcqService.saveBcq(uploadFile, headerList, details.getBuyerIds(), details.getSellerId());
    }

    @GetMapping("/declaration/{headerId}")
    public BcqHeaderDisplay getHeader(@PathVariable long headerId) {
        return new BcqHeaderDisplay(bcqService.findHeader(headerId));
    }

    @GetMapping("/data/{headerId}")
    public List<BcqDataInfo> getData(@PathVariable long headerId) {
        return bcqService.findAllData(headerId)
                .stream()
                .map(BcqDataInfo::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/{headerId}/status")
    public void saveData(@PathVariable long headerId, @RequestBody BcqUpdateStatusDetails updateStatusDetails) {
        bcqService.updateHeaderStatus(headerId, updateStatusDetails);
    }

    @PostMapping("/validation-error")
    public void validationError(@RequestBody Map<String, String> source) {
        String format = "MMM. dd, yyyy hh:mm";
        DateFormat dateFormat = new SimpleDateFormat(format);
        String submittedDate = dateFormat.format(new Date());

        Map<String, Object> payload = new HashMap<>();
        payload.put("recipientId", Long.parseLong(source.get("userId")));
        payload.put("submittedDate", submittedDate);
        payload.put("errorMessage", source.get("errorMessage"));

        BcqEvent eventSeller = new BcqValidationSellerEvent(payload);
        eventPublisher.publishEvent(eventSeller);

        payload.remove("recipientId");
        payload.put("sellerName", source.get("participantName"));
        payload.put("sellerShortName", source.get("participantShortName"));

        BcqEvent eventDept = new BcqValidationDeptEvent(payload);
        eventPublisher.publishEvent(eventDept);
    }
}

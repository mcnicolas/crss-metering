package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public BcqDetailsInfo uploadData(@RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam("sellerShortName") String sellerShortName) throws IOException {

        boolean recordExists = false;
        BcqUploadFile file = new BcqUploadFile();
        file.setFileName(multipartFile.getOriginalFilename());
        file.setFileSize(multipartFile.getSize());

        BcqDetails details = bcqReader.readData(multipartFile.getInputStream());
        details.setFile(file);

        if (details.getHeaderList() != null) {
            details.getHeaderList().forEach(header -> header.setSellingParticipantShortName(sellerShortName));
            recordExists = details.getHeaderList().stream().anyMatch(header -> bcqService.headerExists(header));
            details.setRecordExists(recordExists);
        } else {
            details.setHeaderList(new ArrayList<>());
        }

        return new BcqDetailsInfo(details);
    }

    @PostMapping("/save")
    public void saveData(@RequestBody BcqDetailsInfo detailsInfo) {
        detailsInfo.getFileInfo().setSubmittedDate(new Date());
        bcqService.save(detailsInfo.target());
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
}

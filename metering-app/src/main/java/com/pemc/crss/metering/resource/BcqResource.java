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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.BcqValidationRules.INCOMPLETE_REDECLARATION_ENTRIES;
import static com.pemc.crss.metering.parser.bcq.util.BCQParserUtil.DATE_FORMATS;
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

        BcqUploadFile file = new BcqUploadFile();
        file.setFileName(multipartFile.getOriginalFilename());
        file.setFileSize(multipartFile.getSize());

        BcqDetails details = bcqReader.readData(multipartFile.getInputStream());
        details.setFile(file);

        if (details.getHeaderList() != null) {
            Map<String, String> params = new HashMap<>();
            params.put("sellingParticipant", sellerShortName);
            params.put("tradingDate", formatDate(details.getHeaderList().get(0).getTradingDate()));
            List<BcqHeader> currentHeaderList = bcqService.findAllHeaders(params);

            if (currentHeaderList.size() > 0) {
                details.setRecordExists(true);

                List<BcqHeader> missingHeaderList = currentHeaderList
                        .stream()
                        .filter(header -> !bcqService.isHeaderInList(header, details.getHeaderList()))
                        .collect(Collectors.toList());

                if (missingHeaderList.size() > 0) {
                    final StringBuilder pairList = new StringBuilder();
                    missingHeaderList.forEach(missingHeader -> {
                        if (!pairList.toString().isEmpty()) {
                            pairList.append("\n");
                        }
                        pairList.append(missingHeader.getSellingMtn())
                                .append(", ")
                                .append(missingHeader.getBillingId());
                    });
                    String tradingDate = formatDate(missingHeaderList.get(0).getTradingDate());
                    String errorMessage = String.format(INCOMPLETE_REDECLARATION_ENTRIES.getErrorMessage(),
                            tradingDate, pairList.toString());
                    details.setErrorMessage(errorMessage);
                }
            }
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



    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMATS[0]);
        return dateFormat.format(date);
    }
}

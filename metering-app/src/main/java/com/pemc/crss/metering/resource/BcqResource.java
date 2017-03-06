package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.constants.ValidationStatus;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventForm;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.service.reports.BcqReportService;
import com.pemc.crss.metering.utils.BcqDateUtils;
import com.pemc.crss.metering.utils.DateTimeUtils;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqStatus.*;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequestMapping("/bcq")
public class BcqResource {

    private static final String CSV_CONTENT_TYPE = "text/csv";

    private final BcqReader bcqReader;
    private final BcqValidationHandler validationHandler;
    private final BcqService bcqService;
    private final BcqReportService reportService;
    private final CacheConfigService configService;

    @Autowired
    public BcqResource(final BcqReader bcqReader, final BcqValidationHandler validationHandler,
                       final BcqService bcqService, final BcqReportService reportService,
                       final CacheConfigService configService) {

        this.bcqReader = bcqReader;
        this.validationHandler = validationHandler;
        this.bcqService = bcqService;
        this.reportService = reportService;
        this.configService = configService;
    }

    @PostMapping(value = "/list")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<DataTableResponse<BcqHeaderPageDisplay>> executeSearch(@RequestBody final PageableRequest request) {
        Page<BcqHeaderPageDisplay> headerPage = bcqService.findAllHeaders(request);
        DataTableResponse<BcqHeaderPageDisplay> response = new DataTableResponse<BcqHeaderPageDisplay>()
                .withData(headerPage.getContent())
                .withRecordsTotal(headerPage.getTotalElements());
        return ok(response);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        log.debug("[REST-BCQ] Request for uploading of: {}", multipartFile.getOriginalFilename());
        BcqDeclaration declaration = processAndValidateDeclaration(multipartFile);
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("[REST-BCQ] Finished uploading and rejecting of: {}", multipartFile.getOriginalFilename());
            bcqService.saveSellerDeclaration(declaration);
            return unprocessableEntity().body(declaration.getValidationResult());
        }
        log.debug("[REST-BCQ] Finished uploading of: {}", multipartFile.getOriginalFilename());
        return ok(declaration);
    }

    @PostMapping("/settlement/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> uploadBySettlement(@RequestParam("file") MultipartFile multipartFile,
                                                @RequestPart String sellerDetailsString,
                                                @RequestPart String tradingDateString) throws IOException {
        log.debug("[REST-BCQ] Request for settlement uploading of: {}", multipartFile.getOriginalFilename());
        String[] sellerDetailsArray = sellerDetailsString.split(", ");
        String sellerName = sellerDetailsArray[0];
        String sellerShortName = sellerDetailsArray[1];
        ParticipantSellerDetails sellerDetails = new ParticipantSellerDetails(sellerName, sellerShortName);
        BcqDeclaration declaration = processAndValidateSettlementDeclaration(multipartFile, sellerDetails,
                parseDate(tradingDateString));
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("[REST-BCQ] Finished uploading and rejecting of: {}", multipartFile.getOriginalFilename());
            bcqService.saveSettlementDeclaration(declaration);
            return unprocessableEntity().body(declaration.getValidationResult());
        }
        log.debug("[REST-BCQ] Finished settlement uploading of: {}", multipartFile.getOriginalFilename());
        return ok(declaration);
    }

    @PostMapping("/webservice/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<String> uploadByWebService(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        log.debug("[REST-BCQ] Request for uploading by web service of: {}", multipartFile.getOriginalFilename());
        if (!multipartFile.getContentType().equalsIgnoreCase(CSV_CONTENT_TYPE)) {
            log.debug("[REST-BCQ] Uploading failed, {} is not a CSV file", multipartFile.getOriginalFilename());
            return badRequest().body("Only CSV files are allowed.");
        }
        BcqDeclaration declaration = processAndValidateDeclaration(multipartFile);
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("[REST-BCQ] Finished uploading and rejecting by web service of: {}",
                    multipartFile.getOriginalFilename());
            bcqService.saveSellerDeclaration(declaration);
            return unprocessableEntity().body(removeHtmlTags(
                    declaration.getValidationResult().getErrorMessage().getFormattedMessage()));
        }
        bcqService.saveSellerDeclaration(declaration);
        log.debug("[REST-BCQ] Finished uploading and saving by web service of: {}", multipartFile.getOriginalFilename());
        if (declaration.isResubmission()) {
            return ok("Successfully saved resubmission.");
        }
        return ok("Successfully saved declaration.");
    }

    @PostMapping("/save")
    public void save(@RequestBody BcqDeclaration declaration) throws IOException {
        log.debug("[REST-BCQ] Request for saving declaration");
        bcqService.saveSellerDeclaration(declaration);
        log.debug("[REST-BCQ] Finished saving declaration");
    }

    @PostMapping("/settlement/save")
    public void saveBySettlement(@RequestBody BcqDeclaration declaration) throws IOException {
        log.debug("[REST-BCQ] Request for saving declaration");
        bcqService.saveSettlementDeclaration(declaration);
        log.debug("[REST-BCQ] Finished saving declaration");
    }

    @GetMapping("/declaration/{headerId}")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public BcqHeaderDisplay getHeader(@PathVariable long headerId,
                                      @RequestParam(required = false) boolean searchForLatest,
                                      @RequestParam(required = false) boolean isSettlement) {

        log.debug("[REST-BCQ] Request for getting header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.debug("[REST-BCQ] No found header with ID: {}", headerId);
            return null;
        }
        List<BcqStatus> excludedStatus;
        if (isSettlement) {
            excludedStatus = singletonList(VOID);
        } else {
            excludedStatus = asList(VOID, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE, FOR_APPROVAL_CANCEL);
        }
        header = bcqService.findSameHeadersWithStatusNotIn(header, excludedStatus).get(0);
        long idToSearch = searchForLatest ? header.getHeaderId() : headerId;
        BcqHeaderDisplay headerDisplay = new BcqHeaderDisplay(bcqService.findHeader(idToSearch));
        log.debug("[REST-BCQ] Found header display: {}", headerDisplay);
        return headerDisplay;
    }

    @GetMapping("/declaration/{headerId}/data")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public List<BcqDataDisplay> getData(@PathVariable long headerId) {
        log.debug("[REST-BCQ] Request for getting data of header with ID: {}", headerId);
        List<BcqDataDisplay> dataDisplayList = bcqService.findDataByHeaderId(headerId).stream()
                .map(BcqDataDisplay::new).collect(toList());
        log.debug("[REST-BCQ] Found {} data of header with ID: {}", dataDisplayList.size(), headerId);
        return dataDisplayList;
    }

    @GetMapping("/declaration/{headerId}/previous")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public List<BcqHeaderDisplay> getPrevHeaders(@PathVariable long headerId,
                                                 @RequestParam(required = false) boolean isSettlement) {
        log.debug("[REST-BCQ] Request for getting previous headers of header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.debug("[REST-BCQ] No found header with ID: {}", headerId);
            return null;
        }
        List<BcqStatus> excludedStatus;
        if (isSettlement) {
            excludedStatus = singletonList(VOID);
        } else {
            excludedStatus = asList(VOID, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE, FOR_APPROVAL_CANCEL);
        }
        List<BcqHeader> prevHeaders = bcqService.findSameHeadersWithStatusNotIn(header, excludedStatus);
        List<BcqHeaderDisplay> prevHeadersDisplay = prevHeaders.stream().map(BcqHeaderDisplay::new).collect(toList());
        log.debug("[REST-BCQ] Found {} prev headers display: {}", prevHeadersDisplay.size());
        return prevHeadersDisplay;
    }

    @PostMapping("/declaration/{headerId}/{status}")
    @PreAuthorize("hasAnyAuthority('BCQ_ASSESS_BILATERAL_CONTRACT_QUANTITY', 'BCQ_CANCEL_BILATERAL_CONTRACT_QUANTITY')")
    public void updateStatus(@PathVariable long headerId, @PathVariable String status) {
        log.debug("[REST-BCQ] Request for updating status to {} of header with ID: {}", status, headerId);
        bcqService.updateHeaderStatus(headerId, fromString(status));
        log.debug("[REST-BCQ] Finished updating status to {} of header with ID: {}", status, headerId);
    }

    @PostMapping("/declaration/settlement/{headerId}/{status}")
    @PreAuthorize("hasAuthority('BCQ_CANCEL_BILATERAL_CONTRACT_QUANTITY')")
    public void updateStatusBySettlement(@PathVariable long headerId, @PathVariable String status) {
        log.debug("[REST-BCQ] Request for settlement updating status to {} of header with ID: {}", status, headerId);
        bcqService.updateHeaderStatusBySettlement(headerId, fromString(status));
        log.debug("[REST-BCQ] Finished settlement updating status to {} of header with ID: {}", status, headerId);
    }

    @PostMapping("/declaration/settlement/approve/{headerId}")
    @PreAuthorize("hasAuthority('BCQ_ASSESS_BILATERAL_CONTRACT_QUANTITY')")
    public void approve(@PathVariable long headerId) {
        log.debug("[REST-BCQ] Request for approval of header with ID: {}", headerId);
        bcqService.approve(headerId);
        log.debug("[REST-BCQ] Finished approval of header with ID: {}", headerId);
    }

    @GetMapping("/sellers")
    public List<ParticipantSellerDetails> getSellersByTradingDate(@RequestParam String tradingDate) {
        log.debug("[REST-BCQ] Request for getting sellers with trading date: {}", tradingDate);
        List<ParticipantSellerDetails> sellerDetailsList = bcqService.findAllHeaders(of(
                        "tradingDate", tradingDate,
                        "expired", "expired")
        ).stream().map(header ->
                new ParticipantSellerDetails(header.getSellingParticipantName(),
                        header.getSellingParticipantShortName()))
                .distinct()
                .collect(toList());
        log.debug("[REST-BCQ] Found {} sellers with trading date: {}", sellerDetailsList.size(), tradingDate);
        return sellerDetailsList;
    }

    @PreAuthorize("hasAuthority('BCQ_CREATE_SPECIAL_EVENT')")
    @PostMapping("/special-event/save")
    public void saveSpecialEvent(@RequestBody BcqSpecialEventForm specialEventForm) {
        log.debug("[REST-BCQ] Request for saving special event");
        long eventId = bcqService.saveSpecialEvent(specialEventForm.target());
        log.debug("[REST-BCQ] Saved special event with id: {}", eventId);
    }

    @PreAuthorize("hasAuthority('BCQ_VIEW_SPECIAL_EVENT')")
    @GetMapping("/special-event/list")
    public ResponseEntity<List<BcqSpecialEventList>> getSpecialEvents() {
        log.debug("[REST-BCQ] Request for getSpecialEvents");
        List<BcqSpecialEventList> result = bcqService.getSpecialEvents();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('BCQ_EXPORT')")
    @PostMapping("/data/report")
    public void generateBcqDataReport(@RequestBody Map<String, String> mapParams, HttpServletResponse response)
            throws IOException {
        // Inform frontend just in case
        if (!mapParams.containsKey("tradingDate")) {
            throw new RuntimeException("No Trading Date Provided for BCQ Report Generation");
        }

        final String tradingDate = mapParams.get("tradingDate");
        final String timestamp = BcqDateUtils.getReportFilenameDateFormat(DateTimeUtils.now());

        final String encodeFile = URLEncoder.encode("BCQ_REPORT_" + tradingDate + "_" + timestamp + ".csv", "UTF-8");
        final String filename = URLDecoder.decode(encodeFile, "ISO8859_1");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=" + filename);

        reportService.generateBcqDataReport(mapParams, response.getOutputStream());
    }

    @GetMapping("/template")
    public void getBcqTemplate(final HttpServletResponse response) throws IOException {
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=bcq_template.csv");

        reportService.generateBcqUploadTemplate(response.getOutputStream());
    }

    @GetMapping("/settlement/config")
    public int getAllowableTradingDateConfig() {
        return configService.getIntegerValueForKey("BCQ_ALLOWABLE_TRADING_DATE", 1);
    }


    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private BcqDeclaration processAndValidateDeclaration(MultipartFile multipartFile) throws IOException {
        List<List<String>> csv = bcqReader.readCsv(multipartFile.getInputStream());
        BcqDeclaration declaration = validationHandler.processAndValidate(csv);
        BcqValidationResult validationResult = declaration.getValidationResult();
        BcqUploadFile uploadFile = populateUploadFile(multipartFile, validationResult.getStatus());
        declaration.setUploadFileDetails(new BcqUploadFileDetails(uploadFile));
        if (declaration.getHeaderDetailsList() != null) {
            declaration.setResubmission(getCurrentHeaders(declaration).size() > 0);
        }
        return declaration;
    }

    private BcqDeclaration processAndValidateSettlementDeclaration(MultipartFile multipartFile,
                                                                   ParticipantSellerDetails sellerDetails,
                                                                   Date tradingDate) throws IOException {

        List<List<String>> csv = bcqReader.readCsv(multipartFile.getInputStream());
        BcqDeclaration declaration = validationHandler.processAndValidateForSettlement(csv, sellerDetails, tradingDate);
        BcqValidationResult validationResult = declaration.getValidationResult();
        BcqUploadFile uploadFile = populateUploadFile(multipartFile, validationResult.getStatus());
        declaration.setUploadFileDetails(new BcqUploadFileDetails(uploadFile));
        if (declaration.getHeaderDetailsList() != null) {
            declaration.setResubmission(getCurrentHeaders(declaration).size() > 0);
        }
        return declaration;
    }

    private BcqUploadFile populateUploadFile(MultipartFile multipartFile, ValidationStatus status) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(multipartFile.getOriginalFilename());
        uploadFile.setFileSize(multipartFile.getSize());
        uploadFile.setValidationStatus(status);
        return uploadFile;
    }

    private List<BcqHeader> getCurrentHeaders(BcqDeclaration declaration) {
        return bcqService.findAllHeaders(of(
                "sellingParticipant", declaration.getSellerDetails().getShortName(),
                "tradingDate", formatDate(declaration.getHeaderDetailsList().get(0).getTradingDate())
        ));
    }

    private String removeHtmlTags(String message) {
        return message.replaceAll("<(.*?)>", "");
    }

}

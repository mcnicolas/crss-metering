package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.constants.ValidationStatus;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventForm;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqStatus.VOID;
import static com.pemc.crss.metering.constants.BcqStatus.fromString;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.lang.Long.parseLong;
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

    @Autowired
    public BcqResource(BcqReader bcqReader, BcqValidationHandler validationHandler, BcqService bcqService) {
        this.bcqReader = bcqReader;
        this.validationHandler = validationHandler;
        this.bcqService = bcqService;
    }

    @PostMapping(value = "/list")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<DataTableResponse<BcqHeaderDisplay2>> executeSearch(@RequestBody final PageableRequest request) {
        Page<BcqHeaderDisplay2> headerPage = bcqService.findAllHeaders(request);
        DataTableResponse<BcqHeaderDisplay2> response = new DataTableResponse<BcqHeaderDisplay2>()
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

    @PostMapping(value = "/settlement/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> uploadBySettlement(@RequestParam("file") MultipartFile multipartFile,
                                                @RequestPart String sellerDetailsString,
                                                @RequestPart String tradingDateString) throws IOException {
        log.debug("[REST-BCQ] Request for settlement uploading of: {}", multipartFile.getOriginalFilename());
        String[] sellerDetailsArray = sellerDetailsString.split(", ");
        long sellerUserId = parseLong(sellerDetailsArray[0]);
        String sellerName = sellerDetailsArray[1];
        String sellerShortName = sellerDetailsArray[2];
        ParticipantSellerDetails sellerDetails = new ParticipantSellerDetails(sellerUserId, sellerName,
                sellerShortName);
        BcqDeclaration declaration = processAndValidateDeclaration(multipartFile, sellerDetails,
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
    public BcqHeaderDisplay getHeader(@PathVariable long headerId) {
        log.debug("[REST-BCQ] Request for getting header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.debug("[REST-BCQ] No found header with ID: {}", headerId);
            return null;
        }
        header = bcqService.findSameHeadersWithStatusNotIn(header, singletonList(VOID)).get(0);
        BcqHeaderDisplay headerDisplay = new BcqHeaderDisplay(bcqService.findHeader(header.getHeaderId()));
        log.debug("[REST-BCQ] Found header display: {}", headerDisplay);
        return headerDisplay;
    }

    @GetMapping("/declaration/{headerId}/previous")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public List<BcqHeaderDisplay> getPrevHeaders(@PathVariable long headerId) {
        log.debug("[REST-BCQ] Request for getting previous headers of header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.debug("[REST-BCQ] No found header with ID: {}", headerId);
            return null;
        }
        List<BcqHeader> prevHeaders = bcqService.findSameHeadersWithStatusNotIn(header, singletonList(VOID));
        List<BcqHeaderDisplay> prevHeadersDisplay = prevHeaders.stream().map(BcqHeaderDisplay::new).collect(toList());
        log.debug("[REST-BCQ] Found {} prev headers display: {}", prevHeadersDisplay.size());
        return prevHeadersDisplay;
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

    @PostMapping("/declaration/{headerId}/{status}")
    @PreAuthorize("hasAuthority('BCQ_ASSESS_BILATERAL_CONTRACT_QUANTITY')")
    public void updateStatus(@PathVariable long headerId, @PathVariable String status) {
        log.debug("[REST-BCQ] Request for updating status to {} of header with ID: {}", status, headerId);
        bcqService.updateHeaderStatus(headerId, fromString(status));
        log.debug("[REST-BCQ] Finished updating status to {} of header with ID: {}", status, headerId);
    }

    @PostMapping("/declaration/settlement/{headerId}/{status}")
    @PreAuthorize("hasAuthority('BCQ_ASSESS_BILATERAL_CONTRACT_QUANTITY')")
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
                new ParticipantSellerDetails(header.getSellingParticipantUserId(),
                        header.getSellingParticipantName(),
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

    private BcqDeclaration processAndValidateDeclaration(MultipartFile multipartFile,
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

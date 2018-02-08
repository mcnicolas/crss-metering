package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.constants.ValidationStatus;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.BcqDownloadDto;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqProhibitedPairForm;
import com.pemc.crss.metering.dto.bcq.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqUploadFileDetails;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventForm;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.service.reports.BcqReportService;
import com.pemc.crss.metering.utils.BcqDateUtils;
import com.pemc.crss.metering.utils.DateTimeUtils;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqStatus.fromString;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

@Slf4j
@RestController
@RequestMapping("/bcq")
public class BcqResource {

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

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        log.debug("Request for uploading of: {}", fileName);

        BcqDeclaration declaration = validateCsvAndGetDeclaration(multipartFile);
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("Finished uploading and rejecting of: {}", fileName);
            bcqService.saveDeclaration(declaration, false);
            return unprocessableEntity().body(declaration.getValidationResult());
        }
        log.debug("Finished uploading of: {}", fileName);
        return ok(declaration);
    }

    @PostMapping("/webservice/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<String> uploadByWebService(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        log.debug("Request for uploading by web service of: {}", fileName);

        if (!equalsIgnoreCase(getExtension(fileName), "CSV")) {
            log.debug("Uploading failed, {} is not a CSV file", fileName);
            return badRequest().body("Only CSV files are allowed.");
        }

        BcqDeclaration declaration = validateCsvAndGetDeclaration(multipartFile);
        bcqService.saveDeclaration(declaration, false);
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("Finished uploading and rejecting by web service of: {}", fileName);
            BcqValidationErrorMessage errorMessage = declaration.getValidationResult().getErrorMessage();
            return unprocessableEntity().body(removeHtmlTags(errorMessage.getFormattedMessage()));
        }

        log.debug("Finished uploading and saving of: {}", fileName);
        if (declaration.isResubmission()) {
            return ok("Successfully saved resubmission.");
        }
        return ok("Successfully saved declaration.");
    }

    @PostMapping("/settlement/upload")
    @PreAuthorize("hasAuthority('BCQ_UPLOAD_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> uploadBySettlement(@RequestParam("file") MultipartFile multipartFile,
                                                @RequestParam("sellerDetailsString") String sellerDetailsString,
                                                @RequestParam("tradingDateString") String tradingDateString) throws IOException {

        String fileName = multipartFile.getOriginalFilename();
        log.debug("Request for settlement uploading of: {}", fileName);
        int lastIndexOf = sellerDetailsString.lastIndexOf(", ");
        String sellerName = sellerDetailsString.substring(0, lastIndexOf);
        String sellerShortName = sellerDetailsString.substring(lastIndexOf + 2, sellerDetailsString.length());
        ParticipantSellerDetails sellerDetails = new ParticipantSellerDetails(sellerName, sellerShortName.trim(),"");
        BcqDeclaration declaration = validateCsvAndGetDeclaration(multipartFile, sellerDetails,
                parseDate(tradingDateString));
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("Finished uploading and rejecting of: {}", fileName);
            bcqService.saveDeclaration(declaration, true);
            return unprocessableEntity().body(declaration.getValidationResult());
        }
        log.debug("Finished settlement uploading of: {}", fileName);
        return ok(declaration);
    }

    @PostMapping("/save")
    public void save(@RequestBody BcqDeclaration declaration) throws IOException {
        log.debug("Request for saving declaration");
        bcqService.saveDeclaration(declaration, false);
        log.debug("Finished saving declaration");
    }

    @PostMapping("/settlement/save")
    public void saveBySettlement(@RequestBody BcqDeclaration declaration) throws IOException {
        log.debug("Request for saving declaration");
        bcqService.saveDeclaration(declaration, true);
        log.debug("Finished saving declaration");
    }

    @PostMapping("/declaration/update-status/{headerId}")
    @PreAuthorize("hasAnyAuthority('BCQ_ASSESS_BILATERAL_CONTRACT_QUANTITY', 'BCQ_CANCEL_BILATERAL_CONTRACT_QUANTITY')")
    public void updateStatus(@PathVariable long headerId, @RequestParam String status) {
        log.debug("Request for updating status to {} of header with ID: {}", status, headerId);
        bcqService.updateHeaderStatus(headerId, fromString(status));
        log.debug("Finished updating status to {} of header with ID: {}", status, headerId);
    }

    @PostMapping("/declaration/settlement/request-cancel/{headerId}")
    @PreAuthorize("hasAuthority('BCQ_CANCEL_BILATERAL_CONTRACT_QUANTITY')")
    public void requestForCancellation(@PathVariable long headerId) {
        log.debug("Request for cancellation of header with ID: {}", headerId);
        bcqService.requestForCancellation(headerId);
        log.debug("Finished request for cancellation of header with ID: {}", headerId);
    }

    @PostMapping("/declaration/settlement/approve/{headerId}")
    @PreAuthorize("hasAuthority('BCQ_ASSESS_BILATERAL_CONTRACT_QUANTITY')")
    public void approve(@PathVariable long headerId) {
        log.debug("Request for approval of header with ID: {}", headerId);
        bcqService.approve(headerId);
        log.debug("Finished approval of header with ID: {}", headerId);
    }

    @PreAuthorize("hasAuthority('BCQ_CREATE_SPECIAL_EVENT')")
    @PostMapping("/special-event/save")
    public void saveSpecialEvent(@RequestBody BcqSpecialEventForm specialEventForm) {
        log.debug("Request for saving special event");
        long eventId = bcqService.saveSpecialEvent(specialEventForm.target());
        log.debug("Saved special event with id: {}", eventId);
    }

    @PreAuthorize("hasAuthority('BCQ_VIEW_SPECIAL_EVENT')")
    @GetMapping("/special-event/list")
    public ResponseEntity<List<BcqSpecialEventList>> getSpecialEvents() {
        log.debug("Request for getSpecialEvents");
        List<BcqSpecialEventList> result = bcqService.findAllSpecialEvents();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('BCQ_EXPORT')")
    @PostMapping("/data/report")
    public void generateDataReport(@RequestBody Map<String, String> mapParams, HttpServletResponse response)
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

        reportService.generateDataReport(mapParams, response.getOutputStream());
    }

    @GetMapping("/template")
    public void getTemplate(final HttpServletResponse response) throws IOException {
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=bcq_template.csv");

        reportService.generateTemplate(response.getOutputStream());
    }

    @PostMapping("/download/template")
    public void getSampleTemplate(@RequestBody BcqDownloadDto bcqDownloadDto,
                                  final HttpServletResponse response) throws IOException {

        log.debug("bcqDownloadDto={}", bcqDownloadDto);


        LocalDateTime date = DateTimeUtils.parseDateTime24hr(bcqDownloadDto.getDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String fileName = URLEncoder.encode(bcqDownloadDto.getGenName() + "_" + date.format(formatter) + ".csv", "UTF-8");
        fileName = URLDecoder.decode(fileName, "ISO8859_1");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        Long interval = configService.getLongValueForKey("BCQ_INTERVAL", 5L);

        bcqService.generateCsv(bcqDownloadDto, interval, date, response.getOutputStream());
    }


    @GetMapping("/settlement/config")
    public int getAllowableTradingDateConfig() {
        return configService.getIntegerValueForKey("BCQ_ALLOWABLE_TRADING_DATE", 1);
    }

    @PostMapping("/prohibited/save")
    @PreAuthorize("hasAuthority('BCQ_ADD_PROHIBITED')")
    public void saveProhibitedPair(@RequestBody BcqProhibitedPairForm prohibitedPairForm) {
        log.debug("Request for saving prohibited pair: {}", prohibitedPairForm);
        long id = bcqService.saveProhibitedPair(prohibitedPairForm.target());
        log.debug("Saved prohibited pair with ID: {}", id);
    }

    @PutMapping("/prohibited/{id}/disable")
    @PreAuthorize("hasAuthority('BCQ_DELETE_PROHIBITED')")
    public void disableProhibitedPair(@PathVariable long id) {
        log.debug("Request for disabling prohibited pair with ID: {}", id);
        bcqService.disableProhibitedPair(id);
        log.debug("Disabled prohibited pair with ID: {}", id);
    }

    private BcqDeclaration validateCsvAndGetDeclaration(MultipartFile multipartFile) throws IOException {
        List<List<String>> csv = bcqReader.readCsv(multipartFile.getInputStream());
        BcqDeclaration declaration = validationHandler.processAndValidate(csv);
        BcqValidationResult validationResult = declaration.getValidationResult();
        BcqUploadFile uploadFile = populateUploadFile(multipartFile, validationResult.getStatus());
        declaration.setUploadFileDetails(new BcqUploadFileDetails(uploadFile));
        if (declaration.getHeaderDetailsList() != null) {
            String shortName = declaration.getSellerDetails().getShortName();
            Date tradingDate = declaration.getHeaderDetailsList().get(0).getTradingDate();
            List<BcqHeader> currentHeaders = bcqService.findHeadersOfParticipantByTradingDate(shortName, tradingDate);
            declaration.setResubmission(currentHeaders.size() > 0);
        }
        return declaration;
    }

    private BcqDeclaration validateCsvAndGetDeclaration(MultipartFile multipartFile,
                                                        ParticipantSellerDetails sellerDetails,
                                                        Date tradingDate) throws IOException {

        List<List<String>> csv = bcqReader.readCsv(multipartFile.getInputStream());
        BcqDeclaration declaration = validationHandler.processAndValidateForSettlement(csv, sellerDetails, tradingDate);
        BcqValidationResult validationResult = declaration.getValidationResult();
        BcqUploadFile uploadFile = populateUploadFile(multipartFile, validationResult.getStatus());
        declaration.setUploadFileDetails(new BcqUploadFileDetails(uploadFile));
        if (declaration.getHeaderDetailsList() != null) {
            String shortName = declaration.getSellerDetails().getShortName();
            List<BcqHeader> currentHeaders = bcqService.findHeadersOfParticipantByTradingDate(shortName, tradingDate);
            declaration.setResubmission(currentHeaders.size() > 0);
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

    private String removeHtmlTags(String message) {
        return message.replaceAll("<(.*?)>", "");
    }

}

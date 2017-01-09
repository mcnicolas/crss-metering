package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.constants.ValidationStatus;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.BcqStatus.fromString;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate;
import static java.lang.Long.parseLong;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequestMapping("/bcq")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqResource extends BaseListResource<BcqHeaderDisplay> {

    private static final String CSV_CONTENT_TYPE = "text/csv";

    private final BcqReader bcqReader;
    private final BcqValidationHandler validationHandler;
    private final BcqService bcqService;

    @Override
    public DataTableResponse<BcqHeaderDisplay> executeSearch(PageableRequest request) {
        Page<BcqHeader> headerPage = bcqService.findAllHeaders(request);
        List<BcqHeaderDisplay> headerDisplayList = new ArrayList<>();
        headerPage.getContent().forEach(header -> headerDisplayList.add(new BcqHeaderDisplay(header)));
        return new DataTableResponse<BcqHeaderDisplay>()
                .withData(headerDisplayList)
                .withRecordsTotal(headerPage.getTotalElements());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        log.debug("[REST-BCQ] Request for uploading of: {}", multipartFile.getOriginalFilename());
        BcqDeclaration declaration = processAndValidateDeclaration(multipartFile);
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("[REST-BCQ] Finished uploading and rejecting of: {}", multipartFile.getOriginalFilename());
            bcqService.saveDeclaration(declaration);
            return unprocessableEntity().body(declaration.getValidationResult());
        }
        log.debug("[REST-BCQ] Finished uploading of: {}", multipartFile.getOriginalFilename());
        return ok(declaration);
    }

    @PostMapping(value = "/settlement/upload")
    public ResponseEntity<?> uploadBySettlement(@RequestParam("file") MultipartFile multipartFile,
                                                @RequestPart String sellerDetailsString,
                                                @RequestPart String tradingDateString) throws IOException {
        log.debug("[REST-BCQ] Request for settlement uploading of: {}", multipartFile.getOriginalFilename());
        String[] sellerDetailsArray = sellerDetailsString.split(",");
        long sellerUserId = parseLong(sellerDetailsArray[0]);
        String sellerName = sellerDetailsArray[1];
        String sellerShortName = sellerDetailsArray[2];
        ParticipantSellerDetails sellerDetails = new ParticipantSellerDetails(sellerUserId, sellerName,
                sellerShortName);
        BcqDeclaration declaration = processAndValidateDeclaration(multipartFile, sellerDetails,
                parseDate(tradingDateString));
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            log.debug("[REST-BCQ] Finished uploading and rejecting of: {}", multipartFile.getOriginalFilename());
            //bcqService.saveFailedUploadFile(declaration.getUploadFileDetails().target(), declaration);
            return unprocessableEntity().body(declaration.getValidationResult());
        }
        log.debug("[REST-BCQ] Finished settlement uploading of: {}", multipartFile.getOriginalFilename());
        return ok(declaration);
    }

    @PostMapping("/webservice/upload")
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
            bcqService.saveDeclaration(declaration);
            return unprocessableEntity().body(removeHtmlTags(declaration.getValidationResult().getErrorMessage()));
        }
        bcqService.saveDeclaration(declaration);
        log.debug("[REST-BCQ] Finished uploading and saving by web service of: {}", multipartFile.getOriginalFilename());
        if (declaration.isRedeclaration()) {
            return ok("Successfully saved redeclaration.");
        }
        return ok("Successfully saved declaration.");
    }

    @PostMapping("/save")
    public void save(@RequestBody BcqDeclaration declaration) throws IOException {
        log.debug("[REST-BCQ] Request for saving declaration");
        bcqService.saveDeclaration(declaration);
        log.debug("[REST-BCQ] Finished saving declaration");
    }

    @GetMapping("/declaration/{headerId}")
    public BcqHeaderDisplay getHeader(@PathVariable long headerId) {
        log.debug("[REST-BCQ] Request for getting header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.debug("[REST-BCQ] No found header with ID: {}", headerId);
            return null;
        }
        BcqHeaderDisplay headerDisplay = new BcqHeaderDisplay(bcqService.findHeader(headerId));
        log.debug("[REST-BCQ] Found header display: {}", headerDisplay);
        return headerDisplay;
    }

    @GetMapping("/declaration/{headerId}/data")
    public List<BcqDataDisplay> getData(@PathVariable long headerId) {
        log.debug("[REST-BCQ] Request for getting data of header with ID: {}", headerId);
        List<BcqDataDisplay> dataDisplayList = bcqService.findDataByHeaderId(headerId).stream()
                .map(BcqDataDisplay::new).collect(toList());
        log.debug("[REST-BCQ] Found {} data of header with ID: {}", dataDisplayList.size(), headerId);
        return dataDisplayList;
    }

    @PostMapping("/declaration/{headerId}/{status}")
    public void updateStatus(@PathVariable long headerId, @PathVariable String status) {
        log.debug("[REST-BCQ] Request for updating status to {} of header with ID: {}", status, headerId);
        bcqService.updateHeaderStatus(headerId, fromString(status));
        log.debug("[REST-BCQ] Finished updating status to {} of header with ID: {}", status, headerId);
    }

    @GetMapping("/sellers")
    public List<ParticipantSellerDetails> getSellersByTradingDate(@RequestParam String tradingDate) {
        log.debug("[REST-BCQ] Request for getting sellers with trading date: {}", tradingDate);
        List<ParticipantSellerDetails> sellerDetailsList = bcqService
                .findAllSellersWithExpiredBcqByTradingDate(parseDate(tradingDate));
        log.debug("[REST-BCQ] Found {} sellers with trading date: {}", sellerDetailsList.size(), tradingDate);
        return sellerDetailsList;
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
            declaration.setRedeclaration(getCurrentHeaders(declaration).size() > 0);
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
            declaration.setRedeclaration(getCurrentHeaders(declaration).size() > 0);
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
        return bcqService.findAllHeadersBySellerAndTradingDate(
                declaration.getSellerDetails().getShortName(),
                declaration.getHeaderDetailsList().get(0).getTradingDate());
    }

    private String removeHtmlTags(String message) {
        return message.replaceAll("<(.*?)>", "");
    }

}

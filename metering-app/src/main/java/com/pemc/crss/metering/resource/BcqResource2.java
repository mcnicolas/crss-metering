package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.ValidationStatus;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.BcqUploadFileDetails;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqNotificationService;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.service.BcqService2;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static java.util.UUID.randomUUID;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

@Slf4j
@RestController
@RequestMapping("/bcq2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqResource2 {

    private final BcqReader bcqReader;
    private final BcqValidationHandler validationHandler;
    private final BcqService bcqService;
    private final BcqService2 bcqService2;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        log.debug("[REST-BCQ] Request for uploading of: {}", multipartFile.getOriginalFilename());
        List<List<String>> csv = bcqReader.readCsv(multipartFile.getInputStream());
        BcqDeclaration declaration = validationHandler.processAndValidate(csv);
        BcqValidationResult validationResult = declaration.getValidationResult();
        BcqUploadFile uploadFile = populateUploadFile(multipartFile, validationResult.getStatus());
        if (validationResult.getStatus() == REJECTED) {
            bcqService2.saveFailedUploadFile(uploadFile, declaration);
            return unprocessableEntity().body(validationResult);
        }
        declaration.setUploadFileDetails(new BcqUploadFileDetails(uploadFile));
        declaration.setRedeclaration(getCurrentHeaders(declaration).size() > 0);
        log.debug("[REST-BCQ] Finished uploading of: {}", multipartFile.getOriginalFilename());
        return ok(declaration);
    }

    @PostMapping("/save")
    public void save(@RequestBody BcqDeclaration declaration) throws IOException {
        log.debug("[REST-BCQ] Request for saving declaration");
        bcqService2.saveDeclaration(declaration);
        log.debug("[REST-BCQ] Finished saving declaration");
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private BcqUploadFile populateUploadFile(MultipartFile multipartFile, ValidationStatus status) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(multipartFile.getOriginalFilename());
        uploadFile.setFileSize(multipartFile.getSize());
        uploadFile.setTransactionId(randomUUID().toString());
        uploadFile.setSubmittedDate(new Date());
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

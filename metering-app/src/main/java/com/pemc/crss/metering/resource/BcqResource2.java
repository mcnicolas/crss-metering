package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.ValidationStatus;
import com.pemc.crss.metering.dto.BcqUploadFile;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import com.pemc.crss.metering.parser.bcq.BcqReader;
import com.pemc.crss.metering.service.BcqNotificationService;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_VALIDATION_DEPT;
import static com.pemc.crss.metering.constants.BcqEventCode.NTF_BCQ_VALIDATION_SELLER;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDateTime;
import static java.util.UUID.randomUUID;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

@Slf4j
@RestController
@RequestMapping("/bcq2")
public class BcqResource2 {

    private final BcqReader bcqReader;
    private final BcqValidationHandler validationHandler;
    private final BcqService bcqService;
    private final BcqNotificationService bcqNotificationService;

    @Autowired
    public BcqResource2(BcqReader bcqReader,
                        BcqValidationHandler validationHandler,
                        BcqService bcqService,
                        BcqNotificationService bcqNotificationService) {

        this.bcqReader = bcqReader;
        this.validationHandler = validationHandler;
        this.bcqService = bcqService;
        this.bcqNotificationService = bcqNotificationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        log.debug("[REST-BCQ] Request for uploading of: {}", multipartFile.getOriginalFilename());
        List<List<String>> csv = bcqReader.readCsv(multipartFile.getInputStream());
        BcqDeclaration declaration = validationHandler.processAndValidate(csv);
        BcqValidationResult validationResult = declaration.getValidationResult();
        BcqUploadFile uploadFile = saveUploadFile(multipartFile, validationResult.getStatus());
        if (validationResult.getStatus() == REJECTED) {
            sendValidationNotif(uploadFile, declaration);
            return unprocessableEntity().body(validationResult);
        }
        declaration.setUploadFileId(uploadFile.getFileId());
        log.debug("[REST-BCQ] Finished uploading of: {}", multipartFile.getOriginalFilename());
        return ok(declaration);
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private BcqUploadFile saveUploadFile(MultipartFile multipartFile, ValidationStatus status) {
        BcqUploadFile uploadFile = new BcqUploadFile();
        uploadFile.setFileName(multipartFile.getOriginalFilename());
        uploadFile.setFileSize(multipartFile.getSize());
        uploadFile.setTransactionId(randomUUID().toString());
        uploadFile.setSubmittedDate(new Date());
        uploadFile.setValidationStatus(status);
        uploadFile.setFileId(bcqService.saveUploadFile(uploadFile));
        return uploadFile;
    }

    /****************************************************
     * NOTIF METHODS
     ****************************************************/
    private void sendValidationNotif(BcqUploadFile uploadFile, BcqDeclaration declaration) {
        ParticipantSellerDetails sellerDetails = declaration.getSellerDetails();
        bcqNotificationService.send(NTF_BCQ_VALIDATION_SELLER,
                formatDateTime(uploadFile.getSubmittedDate()),
                declaration.getValidationResult().getErrorMessage(),
                sellerDetails.getUserId());
        bcqNotificationService.send(NTF_BCQ_VALIDATION_DEPT,
                formatDateTime(uploadFile.getSubmittedDate()),
                sellerDetails.getName(),
                sellerDetails.getShortName(),
                declaration.getValidationResult().getErrorMessage());
    }

}

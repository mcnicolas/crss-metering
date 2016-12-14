package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.dto.mq.HeaderParam;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
public class MeteringResource {

    public static final String ROUTING_KEY = "crss.mq.data";

    private final MeterService meterService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MeteringResource(MeterService meterService, RabbitTemplate rabbitTemplate) {

        this.meterService = meterService;
        this.rabbitTemplate = rabbitTemplate;
    }

    //    @PreAuthorize("hasRole('MQ_UPLOAD_METER_DATA')") // TODO: Implement
    @PostMapping(value = "/uploadHeader",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> uploadHeader(@Valid @RequestBody HeaderParam headerParam) {
        log.debug("Received header record fileCount:{} category:{}", headerParam.getFileCount(), headerParam.getCategory());

        // TODO: Validation
        // 2. category should be a valid value refer to UploadType enumeration
        long headerID = meterService.saveHeader(headerParam.getFileCount(), headerParam.getCategory());
        log.debug("Saved manifest header: {}", headerID);

        return ResponseEntity.ok(headerID);
    }

//    @PreAuthorize("hasRole('MQ_UPLOAD_METER_DATA')") // TODO: Implement
    @PostMapping(value = "/uploadFile",
            consumes = MULTIPART_FORM_DATA_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("headerID") Long headerID,
                                         @RequestParam("mspShortName") String mspShortName,
                                         @RequestPart("file") MultipartFile[] multipartFiles) throws IOException {
        log.debug("Received file/s headerID:{} mspShortName:{} fileCount:{}", headerID, mspShortName, multipartFiles.length);

        // TODO: Validate

        for (MultipartFile file : multipartFiles) {
            // TODO: Pass a FileManifest bean instead. Could use a json converter
            Message message = MessageBuilder.withBody(file.getBytes())
                    .setHeader("headerID", headerID)
                    .setHeader("fileName", file.getOriginalFilename())
                    .setHeader("mspShortName", mspShortName)
                    .setHeaderIfAbsent("Content type", file.getContentType())
                    .build();

            log.debug("Received file {} headerID:{} delegating to queue", file.getOriginalFilename(), headerID);

            rabbitTemplate.send("crss.mq", ROUTING_KEY, message);
        }

        return new ResponseEntity<>(null, OK);
    }

    //    @PreAuthorize("hasRole('MQ_UPLOAD_METER_DATA')") // TODO: Implement
    @PostMapping("/uploadTrailer")
    public ResponseEntity<String> uploadTrailer(@RequestParam("headerID") long headerID) {
        log.debug("Received trailer record headerID:{}", headerID);

        return ResponseEntity.ok(meterService.saveTrailer(headerID));
    }

}

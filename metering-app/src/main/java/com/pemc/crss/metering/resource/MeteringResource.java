package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.FileParam;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.HeaderParam;
import com.pemc.crss.metering.dto.mq.TrailerParam;
import com.pemc.crss.metering.exception.ConversionException;
import com.pemc.crss.metering.resource.validator.FileUploadValidator;
import com.pemc.crss.metering.service.CacheService;
import com.pemc.crss.metering.service.MeterService;
import com.pemc.crss.metering.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
public class MeteringResource {

    public static final String ROUTING_KEY = "crss.mq.data";
    private static final String MQ_INTERVAL_KEY = "MQ_INTERVAL";
    private static final String MQ_GATE_CLOSURE_TIME_KEY = "MQ_GATE_CLOSURE_TIME";
    private static final String MQ_ALLOWABLE_TRADING_DATE = "MQ_ALLOWABLE_TRADING_DATE";
    private static final String DEFAULT_CLOSURE_TIME = "08:00";
    private static final DateTimeFormatter TIME_FORMATTER_12 = DateTimeFormatter.ofPattern("hh:mm a");
    private static final String METERING_DEPARTMENT = "METERING";

    private final MeterService meterService;
    private final RabbitTemplate rabbitTemplate;
    private final FileUploadValidator fileUploadValidator;
    private final CacheService cacheService;

    @Autowired
    public MeteringResource(MeterService meterService, RabbitTemplate rabbitTemplate, FileUploadValidator fileUploadValidator,
                            CacheService cacheService) {
        this.meterService = meterService;
        this.rabbitTemplate = rabbitTemplate;
        this.fileUploadValidator = fileUploadValidator;
        this.cacheService = cacheService;
    }

    @PreAuthorize("hasAuthority('MQ_UPLOAD_METER_DATA')")
    @PostMapping(value = "/uploadHeader",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadHeader(@Valid @RequestBody HeaderParam headerParam) throws ParseException {
        log.debug("Received header record: {}", headerParam);

        String closureTime = cacheService.getConfig(MQ_GATE_CLOSURE_TIME_KEY);
        String allowableDate = cacheService.getConfig(MQ_ALLOWABLE_TRADING_DATE);
        if (StringUtils.isBlank(closureTime)) {
            closureTime = DEFAULT_CLOSURE_TIME;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closureDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(closureTime));

        UploadType uploadType = UploadType.valueOf(headerParam.getCategory().toUpperCase());

        String userDepartment = SecurityUtils.getDepartment();
        log.info("userDepartment: {}", userDepartment);
        if (!METERING_DEPARTMENT.equalsIgnoreCase(userDepartment)) {
            if (UploadType.DAILY.equals(uploadType)) {
                if (now.isAfter(closureDateTime)) {
                    return ResponseEntity.badRequest()
                            .contentType(MediaType.TEXT_PLAIN)
                            .body("Unable to upload after gate closure time: " + TIME_FORMATTER_12.format(closureDateTime));
                }
            }
        }

        if (headerParam.getConvertToFiveMin() != null
                && headerParam.getConvertToFiveMin()
                && cacheService.getConfig(MQ_INTERVAL_KEY).equals("15")) {

            throw new ConversionException("Conversion is only available when the configured interval is 5 minutes");
        }

        cacheService.getParticipantUserDetail(headerParam.getMspShortName());
        cacheService.getUserDetail(meterService.getUserName());

        Long headerID = meterService.saveHeader(headerParam, closureTime, allowableDate);
        log.debug("Saved manifest header: {}", headerID);

        return ok(headerID);
    }

    @PreAuthorize("hasAuthority('MQ_UPLOAD_METER_DATA')")
    @PostMapping(value = "/uploadFile",
            consumes = MULTIPART_FORM_DATA_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(@ModelAttribute FileParam fileParam, BindingResult result) throws IOException {
        Long headerID = fileParam.getHeaderID();
        HeaderManifest headerManifest = meterService.getHeader(headerID);
        boolean convertToFiveMin = headerManifest.getConvertedToFiveMin().equals("Y");
        fileParam.setConvertToFiveMin(convertToFiveMin);
        MultipartFile[] multipartFiles = fileParam.getFile();

        log.debug("Received file/s headerID:{} fileCount:{}", headerID, multipartFiles.length);

        fileUploadValidator.validate(fileParam, result);
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.getAllErrors(), BAD_REQUEST);
        }

        Map<String, String> retVal = new HashMap<>();
        List<String> files = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            files.add(file.getOriginalFilename());

            // TODO: Pass a FileManifest bean instead. Could use a json converter
            Message message = MessageBuilder.withBody(file.getBytes())
                    .setHeader("headerID", headerID)
                    .setHeader("fileName", file.getOriginalFilename())
                    .setHeaderIfAbsent("Content type", file.getContentType())
                    .build();

            // TODO: o.s.a.s.c.Jackson2JsonMessageConverter   : Could not convert incoming message with content-type [application/octet-stream]

            log.debug("Received file {} headerID:{} delegating to queue", file.getOriginalFilename(), headerID);

            rabbitTemplate.send("crss.mq", ROUTING_KEY, message);
        }

        retVal.put("files", StringUtils.join(files));
        return ok(retVal);
    }

    @PreAuthorize("hasAuthority('MQ_UPLOAD_METER_DATA')")
    @PostMapping(value = "/uploadTrailer",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> uploadTrailer(@Valid @RequestBody TrailerParam trailerParam) {
        log.debug("Received trailer record headerID:{}", trailerParam.getHeaderID());

        Map<String, String> retVal = new HashMap<>();
        retVal.put("transactionID", meterService.saveTrailer(trailerParam.getHeaderID()));
        return ok(retVal);
    }

    @PreAuthorize("hasAuthority('MQ_UPLOAD_METER_DATA')")
    @GetMapping(value = "/checkStatus/{headerID}")
    public ResponseEntity<List<FileManifest>> checkStatus(@PathVariable Long headerID) {
        List<FileManifest> fileManifestList = meterService.checkStatus(headerID);

        return ok(fileManifestList);
    }

    @PreAuthorize("hasAuthority('MQ_UPLOAD_METER_DATA')")
    @GetMapping(value = "/getHeader/{headerID}")
    public ResponseEntity<HeaderManifest> getHeader(@PathVariable Long headerID) {
        HeaderManifest headerManifest = meterService.getHeader(headerID);

        return ok(headerManifest);
    }

}

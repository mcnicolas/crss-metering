package com.pemc.crss.metering.listener;

import com.google.common.collect.ImmutableMap;
import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.event.FileManifestProcessedEvent;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.MeterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.FileType.MDEF;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MeterQuantityListener {

    private final MeterService meterService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Value("${mq.manifest.upload.notif.target.department}")
    private String targetDepartments[];

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.mq.data", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.mq"),
            key = "crss.mq.data"))
    @Transactional
    public void processMeterQuantityFile(@Header int headerID,
                                         @Header String fileName,
                                         @Header String mspShortName,
                                         @Payload byte[] fileContent) {
        log.debug("Received MQ File. fileName:{} headerID:{} mspShortName:{}",
                fileName, headerID, mspShortName);

        String checksum = getChecksum(fileContent);

        // TODO: Should be coming from the controller instead
        FileManifest fileManifest = new FileManifest();
        fileManifest.setHeaderID(headerID);
        fileManifest.setFileName(fileName);
        fileManifest.setFileType(getFileType(fileName));
        fileManifest.setFileSize(fileContent.length);
        fileManifest.setChecksum(checksum);
        fileManifest.setMspShortName(mspShortName);
        fileManifest.setUploadDateTime(new Date());

        try {
            meterService.processMeterData(fileManifest, fileContent);
            log.debug("Done processing file manifest with id= {}", fileManifest.getFileID());
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // TODO: Explore using DLX/DLQ
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }

        log.debug("Firing transaction bound event with params of header: {}, fileId:{}",
                headerID, fileManifest.getHeaderID());
        // fire a transaction bound event
        eventPublisher.publishEvent(FileManifestProcessedEvent.newInstance(headerID, fileManifest.getFileID()));
    }

    @Async
    @TransactionalEventListener
    public void onFileManifestProcessedListener(FileManifestProcessedEvent event) {
        log.debug("Handling event = {}", event);
        final int headerId = event.getHeaderId();
        if (meterService.isFileProcessingCompleted(headerId)) {
            MeterQuantityReport report = meterService.getReport(headerId);
            DateTimeFormatter defaultPattern = DateTimeFormatter.ofPattern("MMM dd, YYYY hh:mm a");

            Map<String, String> rejectedFiles = meterService.findRejectedFiles(headerId).stream()
                    .collect(Collectors.toMap(FileManifest::getFileName, FileManifest::getErrorDetails));

            Arrays.stream(targetDepartments).forEach((String dept) -> {
                final Notification payload = new Notification("NTF_MQ_UPLOAD", System.currentTimeMillis());
                payload.setRecipientDeptCode(dept);

                // payload.setSenderId(); TODO: sender - as msp user
                payload.setPayload(ImmutableMap.<String, Object>builder()
                        .put("submissionDateTime", report.getUploadDateTime().format(defaultPattern))
                        .put("acceptedFileCount", report.getAcceptedFileCount())
                        .put("rejectedFileCount", report.getRejectedFileCount())
                        .put("rejectedFiles", rejectedFiles)
                        .put("uploadedBy", report.getUploadedBy())
                        .build());
                notificationService.notify(payload);
            });
        }
    }

    private String getChecksum(byte[] fileContent) {
        String retVal = "";

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream inputStream = new DigestInputStream(new ByteArrayInputStream(fileContent), md);
            IOUtils.copy(inputStream, NULL_OUTPUT_STREAM);
            retVal = bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String byteValue = Integer.toHexString(0xFF & aByte);
            hexString.append(byteValue.length() == 2 ? byteValue : "0" + byteValue);
        }
        return hexString.toString();
    }

    private FileType getFileType(String filename) {
        FileType retVal = null;

        String fileExt = FilenameUtils.getExtension(filename);

        if (equalsIgnoreCase(fileExt, "XLS") || equalsIgnoreCase(fileExt, "XLSX")) {
            retVal = XLS;
        } else if (equalsIgnoreCase(fileExt, "MDE") || equalsIgnoreCase(fileExt, "MDEF")) {
            retVal = MDEF;
        } else if (equalsIgnoreCase(fileExt, "CSV")) {
            retVal = FileType.CSV;
        }

        return retVal;
    }

}

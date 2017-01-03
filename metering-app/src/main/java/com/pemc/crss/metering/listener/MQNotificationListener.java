package com.pemc.crss.metering.listener;

import com.google.common.collect.ImmutableMap;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.event.MeterQuantityUploadEvent;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MQNotificationListener implements ApplicationListener<MeterQuantityUploadEvent> {

    private final MeterService meterService;
    private final NotificationService notificationService;

    @Value("${mq.manifest.upload.notif.target.department}")
    private String targetDepartments[];

    public MQNotificationListener(MeterService meterService, NotificationService notificationService) {
        this.meterService = meterService;
        this.notificationService = notificationService;
    }

    @Override
    public void onApplicationEvent(MeterQuantityUploadEvent event) {
        log.debug("Handling event = {}", event);

        long headerID = (Long) event.getSource();

        if (meterService.isFileProcessingCompleted(headerID)) {
            MeterQuantityReport report = meterService.getReport(headerID);
            DateTimeFormatter defaultPattern = DateTimeFormatter.ofPattern("MMM dd, YYYY hh:mm a");

            Map<String, String> rejectedFiles = meterService.findRejectedFiles(headerID).stream()
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

}

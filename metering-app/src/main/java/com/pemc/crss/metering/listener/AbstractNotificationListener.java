package com.pemc.crss.metering.listener;

import com.google.common.collect.ImmutableMap;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class AbstractNotificationListener {

    @Autowired
    private MeterService meterService;

    @Autowired
    private NotificationService notificationService;

    @Value("${mq.manifest.upload.notif.target.department}")
    private String targetDepartments[];

    public void notify(Long headerID) {
        MeterQuantityReport report = meterService.getReport(headerID);
        if (report != null) {
            DateTimeFormatter defaultPattern = DateTimeFormatter.ofPattern("MMM dd, YYYY hh:mm a");

            HeaderManifest headerManifest = meterService.getHeader(headerID);

            List<FileManifest> files = meterService.getAllFileManifest(headerID);

            Map<String, String> acceptedFiles = files.stream()
                    .filter(file -> equalsIgnoreCase(file.getStatus(), "ACCEPTED"))
                    .collect(Collectors.toMap(FileManifest::getFileName, FileManifest::getErrorDetails));

            Map<String, String> rejectedFiles = files.stream()
                    .filter(file -> equalsIgnoreCase(file.getStatus(), "REJECTED"))
                    .collect(Collectors.toMap(FileManifest::getFileName, FileManifest::getErrorDetails));

            Map<String, String> unprocessedFiles = files.stream()
                    .filter(file -> isBlank(file.getStatus()))
                    .collect(Collectors.toMap(FileManifest::getFileName, FileManifest::getErrorDetails));

            Integer unprocessedFileCount = meterService.getUnprocessedFileCount(headerID);
            int totalUnprocessedFileCount = unprocessedFileCount + unprocessedFiles.size();

            Arrays.stream(targetDepartments).forEach((String dept) -> {
                Notification payload = new Notification("NTF_MQ_UPLOAD", System.currentTimeMillis());
                payload.setRecipientDeptCode(dept);

                // payload.setSenderId(); TODO: sender - as msp user
                payload.setPayload(ImmutableMap.<String, Object>builder()
                        .put("transactionID", headerManifest.getTransactionID())
                        .put("submissionDateTime", report.getUploadDateTime().format(defaultPattern))
                        .put("totalFileCount", headerManifest.getFileCount())
                        .put("acceptedFileCount", acceptedFiles.size())
                        .put("acceptedFiles", acceptedFiles)
                        .put("rejectedFileCount", rejectedFiles.size())
                        .put("rejectedFiles", rejectedFiles)
                        .put("unprocessedFileCount", totalUnprocessedFileCount)
                        .put("unprocessedFiles", unprocessedFiles)
                        .put("uploadedBy", report.getUploadedBy())
                        .build());
                notificationService.notify(payload);
            });

            meterService.updateNotificationFlag(headerID);
        }
    }

}

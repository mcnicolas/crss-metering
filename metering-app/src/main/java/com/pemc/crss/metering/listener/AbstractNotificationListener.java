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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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

            Map<String, String> acceptedFiles = new HashMap<>();
            for (FileManifest file : files) {
                if (equalsIgnoreCase(file.getStatus(), "ACCEPTED")) {
                    acceptedFiles.put(file.getFileName(), "");
                }
            }

            Map<String, String> rejectedFiles = new HashMap<>();
            for (FileManifest file : files) {
                if (equalsIgnoreCase(file.getStatus(), "REJECTED")) {
                    rejectedFiles.put(file.getFileName(), isEmpty(file.getErrorDetails()) ? "" : file.getErrorDetails());
                }
            }

            Map<String, String> unprocessedFiles = new HashMap<>();
            for (FileManifest file : files) {
                if (isBlank(file.getStatus())) {
                    unprocessedFiles.put(file.getFileName(), "");
                }
            }

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

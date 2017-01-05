package com.pemc.crss.metering.listener;

import com.google.common.collect.ImmutableMap;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
public class MQCleanupListener {

    private final MeterService meterService;
    private final NotificationService notificationService;

    @Value("${mq.manifest.upload.notif.target.department}")
    private String targetDepartments[];

    @Autowired
    public MQCleanupListener(MeterService meterService, NotificationService notificationService) {
        this.meterService = meterService;
        this.notificationService = notificationService;
    }

    @RabbitListener(bindings = @QueueBinding(key = "crss.scheduler.mq.cleanup",
            value = @Queue(value = "crss.scheduler.mq.cleanup", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.scheduler")))
    public void processMQCleanup() {
        log.debug("Received MQ cleanup trigger.");

        List<Long> staleRecords = meterService.getStaleRecords();
        log.debug("Stale Record count:{}", staleRecords.size());

        for (Long headerID : staleRecords) {
            MeterQuantityReport report = meterService.getReport(headerID);
            if (report != null) {
                // TODO: Duplicated code. Refactor!
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

                meterService.updateNotificationFlag(headerID);
            }
        }
    }

}

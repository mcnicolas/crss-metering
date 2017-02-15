package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.dto.UserDetail;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.dto.mq.ParticipantUserDetail;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.CacheService;
import com.pemc.crss.metering.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class AbstractNotificationListener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, YYYY hh:mm a");

    @Autowired
    private MeterService meterService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CacheService cacheService;

    public void notify(Long headerID) {
        MeterQuantityReport report = meterService.getReport(headerID);
        if (report != null) {
            HeaderManifest headerManifest = meterService.getHeader(headerID);

            List<FileManifest> files = meterService.getAllFileManifest(headerID);

            Map<String, String> acceptedFiles = new HashMap<>();
            Map<String, String> rejectedFiles = new HashMap<>();
            Map<String, String> unprocessedFiles = new HashMap<>();

            for (FileManifest file : files) {
                if (equalsIgnoreCase(file.getStatus(), "ACCEPTED")) {
                    acceptedFiles.put(file.getFileName(), "");
                } else if (equalsIgnoreCase(file.getStatus(), "REJECTED")) {
                    rejectedFiles.put(file.getFileName(), isEmpty(file.getErrorDetails()) ? "" : file.getErrorDetails());
                } else if (isBlank(file.getStatus())) {
                    unprocessedFiles.put(file.getFileName(), "");
                }
            }

            Integer unprocessedFileCount = meterService.getUnprocessedFileCount(headerID);
            int totalUnprocessedFileCount = Math.abs(unprocessedFileCount) + unprocessedFiles.size();

            Map<String, Object> map = new HashMap<>();
            map.put("transactionID", headerManifest.getTransactionID());
            map.put("submissionDateTime", report.getUploadDateTime().format(DATE_TIME_FORMATTER));
            map.put("totalFileCount", headerManifest.getFileCount());
            map.put("acceptedFileCount", acceptedFiles.size());
            map.put("acceptedFiles", acceptedFiles);
            map.put("rejectedFileCount", rejectedFiles.size());
            map.put("rejectedFiles", rejectedFiles);
            map.put("unprocessedFileCount", totalUnprocessedFileCount);
            map.put("unprocessedFiles", unprocessedFiles);

            ParticipantUserDetail participantUserDetail = cacheService.getParticipantUserDetail(headerManifest.getMspShortName());
            Set<Long> userIDSet = participantUserDetail.getAssociatedUserID();

            UserDetail userDetail = cacheService.getUserDetail(report.getUploadedBy());
            if (userDetail.isPemcUser()) {
                map.put("uploadedBy",
                        String.format("%1$s (%2$s)", userDetail.getFullName(), userDetail.getUsername()));
            } else {
                map.put("uploadedBy",
                        String.format("%1$s (%2$s)", participantUserDetail.getParticipantName(), participantUserDetail.getShortName()));
            }

            long now = System.currentTimeMillis();
            notifyDepartment(now, map);
            notifyMSP(now, map, userIDSet);

            meterService.updateNotificationFlag(headerID);
        }
    }

    private void notifyDepartment(long now, Map<String, Object> map) {
        Notification payload = new Notification("NTF_MQ_UPLOAD_DEPT", now);
        payload.setRecipientDeptCode("METERING");

        payload.setPayload(map);
        notificationService.notify(payload);
    }

    private void notifyMSP(long now, Map<String, Object> map, Set<Long> userIDSet) {
        if (isNotEmpty(userIDSet)) {
            userIDSet.forEach(userID -> {
                Notification payload = new Notification("NTF_MQ_UPLOAD_MSP", now);
                payload.setRecipientId(userID);

                payload.setPayload(map);
                notificationService.notify(payload);
            });
        }
    }

}

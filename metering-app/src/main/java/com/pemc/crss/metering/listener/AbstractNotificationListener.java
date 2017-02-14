package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.HeaderManifest;
import com.pemc.crss.metering.dto.mq.MeterQuantityReport;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private CacheManager cacheManager;

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
            map.put("uploadedBy", report.getUploadedBy());

            long now = System.currentTimeMillis();
            notifyDepartment(headerID, now, map);
            notifyMSP(headerID, headerManifest.getMspShortName(), now, map);
        }
    }

    private void notifyDepartment(long headerID, long now, Map<String, Object> map) {
        Notification payload = new Notification("NTF_MQ_UPLOAD_DEPT", now);
        payload.setRecipientDeptCode("METERING");

        payload.setPayload(map);
        notificationService.notify(payload);

        meterService.updateNotificationFlag(headerID);
    }

    private void notifyMSP(long headerID, String mspShortName, long now, Map<String, Object> map) {
        Cache mspCache = cacheManager.getCache("msp");
        Cache.ValueWrapper wrapper = mspCache.get(mspShortName);
        if (wrapper != null) {
            Set<Integer> userIDSet = (Set<Integer>) wrapper.get();

            userIDSet.forEach(userID -> {
                Notification payload = new Notification("NTF_MQ_UPLOAD_MSP", now);
                payload.setRecipientId((long)userID);

                payload.setPayload(map);
                notificationService.notify(payload);
            });
        }

        meterService.updateNotificationFlag(headerID);
    }

}

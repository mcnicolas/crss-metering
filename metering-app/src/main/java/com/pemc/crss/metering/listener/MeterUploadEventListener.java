package com.pemc.crss.metering.listener;

import com.pemc.crss.commons.notification.dto.NotificationDTO;
import com.pemc.crss.metering.event.MeterUploadEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MeterUploadEventListener implements ApplicationListener<MeterUploadEvent> {

    private static final String EXCHANGE_TOPIC = "crss.notification";
    private static final String RK_METERING = "crss.notification.metering";
    private static final String NOTIF_CODE = "NTF_MTR_UPLOAD";

    private final RabbitTemplate rabbitTemplate;

    public MeterUploadEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    // TODO: Verify if uploaded file is received here...
    @Override
    public final void onApplicationEvent(MeterUploadEvent event) {
        // TODO:
        // 1. Determine file type
        // 2. Determine category
        // 3. Parse the file based on type
        // 4. Save file content to the database

        log.debug("Event received: type={}", event.getClass());
        NotificationDTO notificationDTO = generateNotification(event);

        log.debug("generated notification {}", notificationDTO);
        sendNotification(notificationDTO);
    }

    @Async
    protected void sendNotification(NotificationDTO notification) {
        log.debug("NOTIFICATION SENT = {}", notification);
        if (notification != null && StringUtils.isNotBlank(notification.getCode())) {
            rabbitTemplate.convertAndSend(EXCHANGE_TOPIC, RK_METERING, notification);
        }
    }

    private NotificationDTO generateNotification(MeterUploadEvent event) {
        NotificationDTO notificationDTO = new NotificationDTO(NOTIF_CODE, event.getTimestamp());
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        Map<String, Object> payload = new HashMap<>();

        int noOfUploadedFiles = (Integer) source.get("noOfUploadedFiles");
        List<String> uploadedFiles = (List) source.get("uploadedFiles");

        payload.put("noOfUploadedFiles", noOfUploadedFiles);

        int count = 1;

        for(String uploadedFile : uploadedFiles) {
            payload.put("file" + count, uploadedFile);
            count ++;
        }

        notificationDTO.setPayload(payload);
        notificationDTO.setRecipientDeptCode("MSP");

        log.debug("PAYLOAD = {}", notificationDTO.getPayload().toString());

        return notificationDTO;
    }

}


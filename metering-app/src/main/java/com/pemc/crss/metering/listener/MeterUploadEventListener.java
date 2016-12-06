package com.pemc.crss.metering.listener;

import com.pemc.crss.commons.notification.dto.NotificationDTO;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.event.MeterUploadEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class MeterUploadEventListener implements ApplicationListener<MeterUploadEvent> {

    private static final String EXCHANGE_TOPIC = "crss.notification";
    private static final String RK_METERING = "crss.notification.metering";
    private static final String NOTIF_CODE = "NTF_MTR_UPLOAD";

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MeterUploadEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @Override
    public final void onApplicationEvent(MeterUploadEvent event) {
        log.debug("Event received: type={}", event.getClass());
        NotificationDTO notificationDTO = generateNotification(event);

        log.debug("generated notification {}", notificationDTO);
        sendNotification(notificationDTO);
    }

    @Async
    private void sendNotification(NotificationDTO notification) {
        log.debug("NOTIFICATION SENT = {}", notification);
        if (notification != null && isNotBlank(notification.getCode())) {
            rabbitTemplate.convertAndSend(EXCHANGE_TOPIC, RK_METERING, notification);
        }
    }

    private NotificationDTO generateNotification(MeterUploadEvent event) {
        NotificationDTO notificationDTO = new NotificationDTO(NOTIF_CODE, event.getTimestamp());
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        Map<String, Object> payload = new HashMap<>();

        List<FileManifest> uploadedFiles = (List) source.get("uploadedFiles");

        payload.put("noOfUploadedFiles", uploadedFiles.size());

        int count = 1;
        for (FileManifest uploadedFile : uploadedFiles) {
            payload.put("file" + count, uploadedFile.getFileName());
            count ++;
        }

        notificationDTO.setPayload(payload);
        notificationDTO.setRecipientDeptCode("MSP");

        log.debug("PAYLOAD = {}", notificationDTO.getPayload().toString());

        return notificationDTO;
    }

}


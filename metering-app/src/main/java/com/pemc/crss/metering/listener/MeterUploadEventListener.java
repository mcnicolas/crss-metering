package com.pemc.crss.metering.listener;

import com.pemc.crss.commons.notification.dto.NotificationDTO;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.event.MeterUploadEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MeterUploadEventListener implements ApplicationListener<MeterUploadEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(MeterUploadEventListener.class);
    private static final String EXCHANGE_TOPIC = "crss.notification";
    private static final String RK_METERING = "crss.notification.metering";
    private static final String NOTIF_CODE = "NTF_MTR_UPLOAD";

    private final RabbitTemplate rabbitTemplate;

    public MeterUploadEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @Override
    public final void onApplicationEvent(MeterUploadEvent event) {
        LOG.debug("Event received: type={}", event.getClass());
        NotificationDTO notificationDTO = generateNotification(event);
        LOG.debug("generated notification {}", notificationDTO);
        sendNotification(notificationDTO);
    }

    @Async
    protected void sendNotification(NotificationDTO notification) {
        LOG.debug("NOTIFICATION SENT = {}", notification);
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

        LOG.debug("PAYLOAD = {}", notificationDTO.getPayload().toString());

        return notificationDTO;
    }

}


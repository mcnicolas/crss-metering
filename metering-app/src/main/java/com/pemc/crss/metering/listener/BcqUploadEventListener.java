package com.pemc.crss.metering.listener;

import com.pemc.crss.commons.notification.dto.NotificationDTO;
import com.pemc.crss.metering.constants.BcqNotificationRecipient;
import com.pemc.crss.metering.constants.BcqNotificationType;
import com.pemc.crss.metering.event.BcqUploadEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqNotificationRecipient.*;

@Slf4j
@Component
public class BcqUploadEventListener implements ApplicationListener<BcqUploadEvent> {

    private static final String EXCHANGE_TOPIC = "crss.notification";
    private static final String RK_METERING = "crss.notification.metering";

    private final RabbitTemplate rabbitTemplate;

    public BcqUploadEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @Override
    public final void onApplicationEvent(BcqUploadEvent event) {
        log.debug("Event received: type={}", event.getClass());
        NotificationDTO notificationDTO = generateNotification(event);

        log.debug("generated notification {}", notificationDTO);
        sendNotification(notificationDTO);
    }

    @Async
    private void sendNotification(NotificationDTO notification) {
        log.debug("NOTIFICATION SENT = {}", notification);
        if (notification != null && StringUtils.isNotBlank(notification.getCode())) {
            rabbitTemplate.convertAndSend(EXCHANGE_TOPIC, RK_METERING, notification);
        }
    }

    private NotificationDTO generateNotification(BcqUploadEvent event) {
        NotificationDTO notification = new NotificationDTO(event.getEventCode().toString(), event.getTimestamp());
        Map<String, Object> source = (Map<String, Object>) event.getSource();

        switch (event.getNotificationRecipient()) {
            case SELLER:
                notification.setRecipientId((Long) source.get("sellerId"));
                break;
            case BUYER:
                notification.setRecipientId((Long) source.get("buyerId"));
                break;
            case BILLING:
                notification.setRecipientDeptCode("BILLING");
                break;
        }

        notification.setPayload(getPayloadByTypeAndRecipient(event.getNotificationType(),
                event.getNotificationRecipient(), source));

        log.debug("PAYLOAD = {}", notification.getPayload().toString());

        return notification;
    }

    private Map<String, Object> getPayloadByTypeAndRecipient(BcqNotificationType type,
                                                             BcqNotificationRecipient notificationRecipient,
                                                             Map<String, Object> source) {

        Map<String, Object> payload = new HashMap<>();

        switch (type) {
            case SUBMIT:
                payload.put("submittedDate", source.get("submittedDate"));

                if (notificationRecipient == SELLER) {
                    payload.put("recordCount", source.get("recordCount"));
                } else if (notificationRecipient == BUYER) {
                    payload.put("sellerName", source.get("sellerName"));
                    payload.put("sellerShortName", source.get("sellerShortName"));
                    payload.put("headerId", source.get("headerId"));
                }
                break;
            case VALIDATION:
                payload.put("submittedDate", source.get("submittedDate"));
                payload.put("errorMessage", source.get("errorMessage"));

                if (notificationRecipient == BILLING) {
                    payload.put("sellerName", source.get("sellerName"));
                    payload.put("sellerShortName", source.get("sellerShortName"));
                }
                break;
            case CANCEL:
                payload.put("tradingDate", source.get("tradingDate"));
                payload.put("respondedDate", source.get("respondedDate"));
                payload.put("sellerName", source.get("sellerName"));
                payload.put("sellerShortName", source.get("sellerShortName"));
                payload.put("headerId", source.get("headerId"));
        }

        return payload;
    }
}

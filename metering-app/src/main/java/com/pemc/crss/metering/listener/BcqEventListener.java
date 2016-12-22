package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.event.BcqEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BcqEventListener implements ApplicationListener<BcqEvent> {

    private static final String EXCHANGE_TOPIC = "crss.notification";
    private static final String RK_METERING = "crss.notification.metering";

    private final RabbitTemplate rabbitTemplate;

    public BcqEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @Override
    public final void onApplicationEvent(BcqEvent event) {
        log.debug("Event received: type={}", event.getClass());
        Notification notification = event.generateNotification();

        log.debug("generated notification {}", notification);
        sendNotification(notification);
    }

    @Async
    private void sendNotification(Notification notification) {
        log.debug("NOTIFICATION SENT = {}", notification);
        if (notification != null && StringUtils.isNotBlank(notification.getCode())) {
            rabbitTemplate.convertAndSend(EXCHANGE_TOPIC, RK_METERING, notification);
        }
    }

}

package com.pemc.crss.metering.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void notify(Notification payload) {
        log.debug("Sending notification={}", payload);
        rabbitTemplate.convertAndSend("crss.notification", "crss.notification.metering", payload );
    }

}

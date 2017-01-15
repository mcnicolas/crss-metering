package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.event.BcqEvent;
import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqEventListener implements ApplicationListener<BcqEvent> {

    private final NotificationService notificationService;

    @Override
    public void onApplicationEvent(BcqEvent event) {
        log.debug("Handling event = {}", event);
        notificationService.notify((Notification) event.getSource());
    }

}

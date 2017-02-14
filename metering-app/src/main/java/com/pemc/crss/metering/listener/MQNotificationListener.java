package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.event.MeterQuantityUploadEvent;
import com.pemc.crss.metering.notification.NotificationService;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQNotificationListener extends AbstractNotificationListener
        implements ApplicationListener<MeterQuantityUploadEvent> {

    private final MeterService meterService;
    private final NotificationService notificationService;

    @Autowired
    public MQNotificationListener(MeterService meterService, NotificationService notificationService) {
        this.meterService = meterService;
        this.notificationService = notificationService;
    }

    @Override
    public void onApplicationEvent(MeterQuantityUploadEvent event) {
        log.debug("Handling event = {}", event);

        long headerID = (Long) event.getSource();

        if (meterService.isFileProcessingCompleted(headerID)) {
            notify(headerID);
        }
    }

}

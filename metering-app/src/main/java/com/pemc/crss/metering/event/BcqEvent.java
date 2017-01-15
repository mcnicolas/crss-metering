package com.pemc.crss.metering.event;

import com.pemc.crss.metering.notification.Notification;
import org.springframework.context.ApplicationEvent;

public class BcqEvent extends ApplicationEvent {

    public BcqEvent(Notification notification) {
        super(notification);
    }

}

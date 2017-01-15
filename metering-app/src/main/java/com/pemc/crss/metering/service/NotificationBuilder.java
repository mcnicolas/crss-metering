package com.pemc.crss.metering.service;

import com.pemc.crss.metering.notification.Notification;

import java.util.HashMap;

import static java.lang.System.currentTimeMillis;

public class NotificationBuilder {

    private Notification notification;

    public NotificationBuilder() {
        notification = new Notification(null, 0);
    }

    public NotificationBuilder withCode(String code) {
        notification.setCode(code);
        return this;
    }

    public NotificationBuilder withSenderId(long senderId) {
        notification.setSenderId(senderId);
        return this;
    }

    public NotificationBuilder withRecipientId(long recipientId) {
        notification.setRecipientId(recipientId);
        return this;
    }

    public NotificationBuilder withRecipientDeptCode(String deptCode) {
        notification.setRecipientDeptCode(deptCode);
        return this;
    }

    public NotificationBuilder addLoad(String key, Object value) {
        if (value == null) {
            return this;
        }
        if (notification.getPayload() == null) {
            notification.setPayload(new HashMap<>());
        }
        notification.getPayload().put(key, value);
        return this;
    }

    public Notification build() {
        notification.setSerial(currentTimeMillis());
        return notification;
    }

}

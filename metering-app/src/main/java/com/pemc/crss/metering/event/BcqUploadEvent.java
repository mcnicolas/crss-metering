package com.pemc.crss.metering.event;

import com.pemc.crss.metering.constants.BcqNotificationRecipient;
import com.pemc.crss.metering.constants.BcqNotificationType;
import com.pemc.crss.metering.constants.BcqUploadEventCode;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class BcqUploadEvent extends ApplicationEvent {

    private BcqUploadEventCode eventCode;
    private BcqNotificationType notificationType;
    private BcqNotificationRecipient notificationRecipient;

    public BcqUploadEvent(Map<String, Object> source) {
        super(source);
    }

    public BcqUploadEvent(Map<String, Object> source,
                          BcqUploadEventCode eventCode,
                          BcqNotificationType notificationType,
                          BcqNotificationRecipient notificationRecipient) {

        super(source);
        this.eventCode = eventCode;
        this.notificationType = notificationType;
        this.notificationRecipient = notificationRecipient;
    }

    public BcqUploadEventCode getEventCode() {
        return eventCode;
    }

    public void setEventCode(BcqUploadEventCode eventCode) {
        this.eventCode = eventCode;
    }

    public BcqNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(BcqNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public BcqNotificationRecipient getNotificationRecipient() {
        return notificationRecipient;
    }

    public void setNotificationRecipient(BcqNotificationRecipient notificationRecipient) {
        this.notificationRecipient = notificationRecipient;
    }
}

package com.pemc.crss.metering.event;

import com.pemc.crss.metering.notification.Notification;
import com.pemc.crss.metering.constants.BcqEventCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Slf4j
public abstract class BcqEvent extends ApplicationEvent {

    private static final String DEPT_BILLING = "BILLING";

    protected final Map<String, Object> source;
    private final BcqEventCode eventCode;

    public BcqEvent(Map<String, Object> source, BcqEventCode eventCode) {
        super(source);
        this.source = source;
        this.eventCode = eventCode;
    }

    public Notification generateNotification() {
        log.debug("Generating BCQ Notification");
        Notification notification = new Notification(eventCode.toString(), getTimestamp());
        notification.setPayload(getPayload());

        if (source.get("recipientId") == null) {
            notification.setRecipientDeptCode(DEPT_BILLING);
        } else {
            notification.setRecipientId((Long) source.get("recipientId"));
        }

        log.debug("Generated BCQ Notification: {}", notification);
        return notification;
    }

    protected abstract Map<String, Object> getPayload();

}

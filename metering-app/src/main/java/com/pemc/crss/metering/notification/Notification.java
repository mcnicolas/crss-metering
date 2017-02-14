package com.pemc.crss.metering.notification;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class Notification {
    // TODO: check for correlated id if it was needed here
    /**
     * must bind to {@code com.pemc.crss.admin.notification.model.NotificationConfigModel.code}
     */
    private String code;

    private Long serial;

    private String recipientDeptCode;
    /**
     * must be existing User
     *
     * @see com.pemc.crss.admin.sec.model.UserModel#id
     */
    private Long senderId;
    /**
     * must be existing User.
     *
     * @see com.pemc.crss.admin.sec.model.UserModel#id
     * <p>
     * This will  be lookup depends on the config
     * @see com.pemc.crss.admin.notification.model.NotificationConfigModel#recipient
     */
    private Long recipientId;
    /**
     * The payload of the body,
     */
    private Map<String, Object> payload = new HashMap<>(); // param name :  value pair and must param must match with
    // NotificationConfigModel.parameters


    public Notification(String code, long timestamp) {
        this.code = code;
        this.serial = timestamp;
    }

}

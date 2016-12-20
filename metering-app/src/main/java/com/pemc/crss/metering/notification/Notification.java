package com.pemc.crss.metering.notification;

import com.google.common.base.MoreObjects;

import java.util.HashMap;
import java.util.Map;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getSerial() {
        return serial;
    }

    public void setSerial(Long serial) {
        this.serial = serial;
    }

    public String getRecipientDeptCode() {
        return recipientDeptCode;
    }

    public void setRecipientDeptCode(String recipientDeptCode) {
        this.recipientDeptCode = recipientDeptCode;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("senderId", senderId)
                .add("recipientId", recipientId)
                .add("recipientDeptCode", recipientDeptCode)
                .add("payload", payload)
                .toString();
    }
}

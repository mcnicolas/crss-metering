package com.pemc.crss.commons.notification.dto;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NotificationDTO implements Serializable {
    private String code;
    private Long serial;
    private String recipientDeptCode;
    private Long senderId;
    private Long recipientId;
    private Map<String, Object> payload = new HashMap<>();

    public NotificationDTO(String code, long timestamp) {
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

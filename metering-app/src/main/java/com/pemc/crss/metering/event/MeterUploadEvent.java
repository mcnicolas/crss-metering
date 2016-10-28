package com.pemc.crss.metering.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class MeterUploadEvent extends ApplicationEvent {

    public MeterUploadEvent(Map<String, Object> source) {
        super(source);
    }
}

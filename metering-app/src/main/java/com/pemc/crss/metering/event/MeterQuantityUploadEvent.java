package com.pemc.crss.metering.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class MeterQuantityUploadEvent extends ApplicationEvent {

    public MeterQuantityUploadEvent(Long source) {
        super(source);
    }

}

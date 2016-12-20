package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeterQuantityReport {
    private long headerId;
    private LocalDateTime uploadDateTime;
    private int acceptedFileCount;
    private int rejectedFileCount;
    private String uploadedBy;
}

package com.pemc.crss.meter.upload;

import lombok.Data;

import java.nio.file.Path;

@Data
public class FileBean {

    private int key;
    private long fileID;
    private Path path;
    private long size;
    private String elapsedTime;
    private String status;
    private String errorDetails;

}

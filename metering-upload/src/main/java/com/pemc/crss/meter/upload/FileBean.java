package com.pemc.crss.meter.upload;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

@Data
public class FileBean {

    private int key;
    private Path path;
    private FileTime lastModified;
    private long size;
    private String checksum;
    private String status;

}

package com.pemc.crss.meter.upload;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class FileBean {

    private Path path;
    private FileTime lastModified;
    private long size;
    private String checksum;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public FileTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(FileTime lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}

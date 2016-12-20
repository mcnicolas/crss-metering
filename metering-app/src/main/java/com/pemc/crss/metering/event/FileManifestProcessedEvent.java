package com.pemc.crss.metering.event;

import com.google.common.base.MoreObjects;

public class FileManifestProcessedEvent {
    private int headerId;
    private Long fileId;

    public FileManifestProcessedEvent(int headerId, Long fileId) {
        this.headerId = headerId;
        this.fileId = fileId;
    }

    public static FileManifestProcessedEvent newInstance(int headerId, Long fileId){
        return new FileManifestProcessedEvent(headerId, fileId);
    }

    public int getHeaderId() {
        return headerId;
    }

    public Long getFileId() {
        return fileId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("headerId", headerId)
                .add("fileId", fileId)
                .toString();
    }
}

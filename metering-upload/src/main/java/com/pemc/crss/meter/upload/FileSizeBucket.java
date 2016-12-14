package com.pemc.crss.meter.upload;

import java.util.List;

// TODO: Implement
public class FileSizeBucket implements FileBucket {

    @Override
    public boolean hasMoreElements() {
        return false;
    }

    @Override
    public List<FileBean> getFiles() {
        return null;
    }

}

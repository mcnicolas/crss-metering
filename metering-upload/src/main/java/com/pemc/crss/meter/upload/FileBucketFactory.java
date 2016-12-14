package com.pemc.crss.meter.upload;

import java.util.List;

public class FileBucketFactory {

    public FileBucket createBucket(List<FileBean> selectedFiles) {
        return new FileCountBucket(selectedFiles);
    }

}

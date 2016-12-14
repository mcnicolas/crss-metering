package com.pemc.crss.meter.upload;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileCountBucket implements FileBucket {

    public static final int DEFAULT_FILECOUNT = 3;

    private final Queue<FileBean> queue;

    private int fileCount = DEFAULT_FILECOUNT;

    public FileCountBucket(List<FileBean> selectedFiles) {
        queue = new LinkedList<>(selectedFiles);
    }

    @Override
    public List<FileBean> getFiles() {
        List<FileBean> retVal = new ArrayList<>();

        int counter = 0;
        while (hasMoreElements() && counter <= fileCount) {
            retVal.add(queue.poll());
            counter++;
        }

        return retVal;
    }

    @Override
    public boolean hasMoreElements() {
        return queue.peek() != null;
    }

}

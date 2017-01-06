package com.pemc.crss.meter.upload;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileSizeBucket implements FileBucket {

    public static final int TWO_MB = 2097152;
    public static final int THREE_MB = 3145728;
    public static final int FOUR_MB = 4194304;
    public static final int FIVE_MB = 5242880;
    public static final int MAX_SIZE = FOUR_MB;

    private final Queue<FileBean> queue;

    private int maxSize = MAX_SIZE;

    public FileSizeBucket(List<FileBean> selectedFiles) {
        queue = new LinkedList<>(selectedFiles);
    }

    @Override
    public List<FileBean> getFiles() {
        List<FileBean> retVal = new ArrayList<>();

        int totalSize = 0;
        while (hasMoreElements() && totalSize <= maxSize) {
            if ((totalSize + queue.peek().getSize()) <= maxSize) {
                FileBean fileBean = queue.poll();
                retVal.add(fileBean);
                totalSize += fileBean.getSize();
            } else {
                break;
            }
        }

        return retVal;
    }

    @Override
    public boolean hasMoreElements() {
        return queue.peek() != null;
    }

}

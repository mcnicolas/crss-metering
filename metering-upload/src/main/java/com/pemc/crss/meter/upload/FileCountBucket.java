package com.pemc.crss.meter.upload;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileCountBucket implements FileBucket {

    public static final int DEFAULT_FILECOUNT = 2;

    public static final int ONE_MB = 1048576;
    public static final int TWO_MB = 2097152;
    public static final int THREE_MB = 3145728;
    public static final int FOUR_MB = 4194304;
    public static final int FIVE_MB = 5242880;
    public static final int SIX_MB = 6291456;
    public static final int MAX_SIZE = FIVE_MB;

    private final Queue<FileBean> queue;

    private int fileCount = DEFAULT_FILECOUNT;

    public FileCountBucket(List<FileBean> selectedFiles) {
        queue = new LinkedList<>(selectedFiles);
    }

    @Override
    public List<FileBean> getFiles() throws LargeFileException {
        List<FileBean> retVal = new ArrayList<>();

        int size = 0;
        int counter = 0;
        while (hasMoreElements() && counter < fileCount) {
            FileBean fileBean = queue.peek();

            if ((size + fileBean.getSize()) <= MAX_SIZE) {
                retVal.add(queue.poll());

                size += fileBean.getSize();
                counter++;

                if (fileBean.getSize() >= ONE_MB) {
                    break;
                }
            } else {
                break;
            }
        }

        if (counter == 0 && size == 0 && queue.size() > 0) {
            throw new LargeFileException("xxx");
        }

        return retVal;
    }

    @Override
    public boolean hasMoreElements() {
        return queue.peek() != null;
    }

}

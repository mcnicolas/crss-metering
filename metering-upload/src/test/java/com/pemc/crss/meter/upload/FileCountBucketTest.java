package com.pemc.crss.meter.upload;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pemc.crss.meter.upload.FileCountBucket.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileCountBucketTest {

    @Test
    public void shouldFillBucketWithCorrectBeanCount() throws LargeFileException {
        // given
        List<FileBean> fileList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            fileList.add(new FileBean());
        }

        FileBucket fileBucket = new FileCountBucket(fileList);

        // when
        int count = 0;
        while (fileBucket.hasMoreElements()) {
            fileBucket.getFiles();
            count++;
        }

        // then
        int expectedBucketFetches = fileList.size() / DEFAULT_FILECOUNT;
        assertThat(count, is(equalTo(expectedBucketFetches)));
    }

    @Test
    public void shouldFillBucketForSmallFiles() throws LargeFileException {
        // given
        List<FileBean> fileList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            FileBean fileBean = new FileBean();
            fileBean.setSize(100);

            fileList.add(fileBean);
        }

        FileBucket fileBucket = new FileCountBucket(fileList);

        // when
        int count = 0;
        while (fileBucket.hasMoreElements()) {
            fileBucket.getFiles();
            count++;
        }

        // then
        int expectedBucketFetches = fileList.size() / DEFAULT_FILECOUNT;
        assertThat(count, is(equalTo(expectedBucketFetches)));
    }

    // TODO: Improvements
// 1. Check total size of bucket
// 2. If greater than the maximum, lessen number of files
// 3. Repeat reorganization until there is only one file in the bucket
// 3. If single file is still greater than the maximum throw an error

    @Test
    public void shouldFillBucketForLargeFiles() throws LargeFileException {
        // given
        List<FileBean> fileList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            FileBean fileBean = new FileBean();
            fileBean.setSize(THREE_MB);

            fileList.add(fileBean);
        }

        FileBucket fileBucket = new FileCountBucket(fileList);

        // when
        while (fileBucket.hasMoreElements()) {
            fileBucket.getFiles();
        }
    }

    @Test(expected = LargeFileException.class)
    public void shouldNotFillBucketForOversizedFile() throws LargeFileException {
        // given
        List<FileBean> fileList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            FileBean fileBean = new FileBean();
            fileBean.setSize(ELEVEN_MB);

            fileList.add(fileBean);
        }

        FileBucket fileBucket = new FileCountBucket(fileList);

        // when
        while (fileBucket.hasMoreElements()) {
            fileBucket.getFiles();
        }
    }

}

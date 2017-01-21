package com.pemc.crss.meter.upload;

import java.util.List;

public interface FileBucket {

    boolean hasMoreElements();

    List<FileBean> getFiles() throws LargeFileException;

}

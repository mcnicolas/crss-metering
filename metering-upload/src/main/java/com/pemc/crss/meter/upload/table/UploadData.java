package com.pemc.crss.meter.upload.table;

import com.pemc.crss.meter.upload.FileBean;
import lombok.Data;

import java.util.List;

@Data
public class UploadData {

    private UploadType uploadType;
    private Long headerID;
    private List<FileBean> fileList;

}

package com.pemc.crss.meter.upload.table;

import com.pemc.crss.meter.upload.FileBean;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UploadData {

    private UploadType uploadType;
    private Long headerID;
    private LocalDateTime uploadStartTime;
    private List<FileBean> fileList;

}

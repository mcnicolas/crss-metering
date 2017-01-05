package com.pemc.crss.metering.dto.mq;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileParam {

    private Long headerID;
    private String mspShortName;
    private String fileType;
    private MultipartFile[] file;

}

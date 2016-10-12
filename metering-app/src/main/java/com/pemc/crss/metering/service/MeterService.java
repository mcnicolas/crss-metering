package com.pemc.crss.metering.service;

import com.pemc.crss.metering.constants.UploadType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

public interface MeterService {

    void saveMeterData(Collection<MultipartFile> values, UploadType uploadType) throws IOException;

}

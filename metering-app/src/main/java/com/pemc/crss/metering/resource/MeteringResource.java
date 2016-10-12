package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class MeteringResource {

    @Autowired
    private MeterService meterService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMeterData(MultipartHttpServletRequest request,
                                                  @RequestParam("uploadType") UploadType uploadType) throws IOException {

        meterService.saveMeterData(request.getFileMap().values(), uploadType);

        return new ResponseEntity<>("Successfully parsed", OK);
    }

}

package com.pemc.crss.metering.bcq.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class BCQResource {

    @PostMapping("/bcq/upload")
    public ResponseEntity<String> uploadBCQ(MultipartHttpServletRequest request) throws IOException {

        System.out.println(request.getMultiFileMap().values().size());

        return new ResponseEntity<>("Successfully parsed", OK);
    }
}

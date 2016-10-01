package com.pemc.crss.metering;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NOTE: This is a test controller to verify service discovery.
 */
@RestController
public class MeterUploadResource {

    @Value("${spring.datasource.url}")
    private String jdbcURL;

    @RequestMapping("/hello")
    public String helloWorld() {
        return "Hello World";
    }

    @RequestMapping("/jdbc")
    public String getjdbc() {
        return jdbcURL;
    }

}

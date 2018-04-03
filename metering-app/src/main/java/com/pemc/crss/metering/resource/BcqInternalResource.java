package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.service.BcqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/__internal__/bcqTemplate")
public class BcqInternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(BcqInternalResource.class);
    private final CacheConfigService configService;
    private final BcqService bcqService;

    @Autowired
    public BcqInternalResource(CacheConfigService configService, BcqService bcqService) {
        this.configService = configService;
        this.bcqService = bcqService;
    }

    @GetMapping("download/{shortName}")
    public void downloadTemplate(@PathVariable String shortName, final HttpServletResponse response) throws IOException {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fileName = URLEncoder.encode(shortName + "_" + date.format(formatter) + ".csv", "UTF-8");
        fileName = URLDecoder.decode(fileName, "ISO8859_1");
        response.setContentType("application/x-msdownload");
       // response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.addHeader("Content-disposition", "attachment; filename=" + fileName);
        Long interval = configService.getLongValueForKey("BCQ_INTERVAL", 5L);
        bcqService.generateInternalCsv(shortName, interval, date, response.getOutputStream());
    }

}

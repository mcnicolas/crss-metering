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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestController
@RequestMapping("/__internal__")
public class BcqInternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(BcqInternalResource.class);
    private final CacheConfigService configService;
    private final BcqService bcqService;

    @Autowired
    public BcqInternalResource(CacheConfigService configService, BcqService bcqService) {
        this.configService = configService;
        this.bcqService = bcqService;
    }

    @GetMapping("/bcqTemplate/download")
    public void downloadTemplate(@RequestParam String shortName, final HttpServletResponse response) throws IOException {
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

    @PostMapping("/bcqSubmission")
    public void downloadBcqSubmission(@RequestParam String shortName,
                                      @RequestParam String date,
                                      @RequestParam String status,
                                      final HttpServletResponse response) throws IOException {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat df2 = new SimpleDateFormat("yyyyMMdd_");
            Date tradingDate = df.parse(date);
            String fileName = URLEncoder.encode(shortName + "_" + df2.format(tradingDate) + System.currentTimeMillis() + ".json", "UTF-8");
            fileName = URLDecoder.decode(fileName, "ISO8859_1");
            response.setContentType("application/x-msdownload");
            // response.setHeader("Content-disposition", "attachment; filename=" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.addHeader("Content-disposition", "attachment; filename=" + fileName);
            if (status.toUpperCase().equals("ALL") || status.equals("SETTLEMENT_READY")) {
                bcqService.generateJsonBcqSubmission(shortName, tradingDate, status, response.getOutputStream());
            } else {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}

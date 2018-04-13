package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.commons.security.SecurityUtil;
import com.pemc.crss.metering.dao.UserTpDao;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final UserTpDao userTpDao;

    @Autowired
    public BcqInternalResource(CacheConfigService configService, BcqService bcqService, UserTpDao userTpDao) {
        this.configService = configService;
        this.bcqService = bcqService;
        this.userTpDao = userTpDao;
    }

    @GetMapping("/bcqTemplate/download")
    public void downloadTemplate(final HttpServletResponse response) throws IOException {
        String shortName = userTpDao.findBShortNameByTpId(SecurityUtils.getUserId().longValue());
        LocalDateTime date = LocalDateTime.now().minusDays(1);
       /* Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String shortName = SecurityUtil.getCurrentUser(auth);*/

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fileName = URLEncoder.encode(shortName + "_" + date.format(formatter) + ".csv", "UTF-8");
        fileName = URLDecoder.decode(fileName, "ISO8859_1");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        Long interval = configService.getLongValueForKey("BCQ_INTERVAL", 5L);
        bcqService.generateInternalCsv(shortName, interval, date, response);
    }

    @PostMapping("/bcqSubmission")
    public void downloadBcqSubmission(@RequestParam String date,
                                      @RequestParam String status,
                                      final HttpServletResponse response) throws IOException {
        try {
             String shortName = userTpDao.findBShortNameByTpId(SecurityUtils.getUserId().longValue());
            /*Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String shortName = SecurityUtil.getCurrentUser(auth);*/
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat df2 = new SimpleDateFormat("yyyyMMdd_");
            DateFormat runtimeFormat = new SimpleDateFormat(" yyyyMMddhhmmss");
            Date tradingDate = df.parse(date);
            String fileName = URLEncoder.encode(shortName + "_" + df2.format(tradingDate) + runtimeFormat.format(new Date())
                    + ".json", "UTF-8");
            fileName = URLDecoder.decode(fileName, "ISO8859_1");
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            if (status.toUpperCase().equals("ALL") || status.equals("SETTLEMENT_READY")) {
                bcqService.generateJsonBcqSubmission(shortName, tradingDate, status, response);
            } else {
                fileName = URLEncoder.encode(shortName + "_error_" + df2.format(tradingDate) + runtimeFormat.format(new Date())
                        + ".txt", "UTF-8");
                fileName = URLDecoder.decode(fileName, "ISO8859_1");
                response.setHeader("Content-disposition", "attachment; filename=" + fileName);

                throw new IllegalArgumentException("Invalid status: " + status);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}

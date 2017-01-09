package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static java.util.Calendar.MINUTE;

@Slf4j
@Component
@Order(value = 5)
public class CorrectIntervalValidator implements Validator {

    private static final int DEFAULT_INTERVAL = 15;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    private final CacheManager cacheManager;

    @Autowired
    public CorrectIntervalValidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        FileType fileType = fileManifest.getFileType();
        if ((fileType == XLS || fileType == CSV)
                && fileManifest.getUploadType() == DAILY) {
            Queue<MeterDataDetail> queue = new LinkedList<>(meterData.getDetails());
            long firstRecord = queue.poll().getReadingDateTime();

            Calendar expectedDateTime = Calendar.getInstance();
            try {
                expectedDateTime.setTime(DATE_FORMAT.parse(String.valueOf(firstRecord)));
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }

            int interval = getInterval();

            while (queue.peek() != null) {
                expectedDateTime.add(MINUTE, interval);

                long expected = Long.parseLong(DATE_FORMAT.format(expectedDateTime.getTime()));
                long actual = queue.poll().getReadingDateTime();

                if (expected != actual) {
                    retVal.setStatus(REJECTED);
                    retVal.setErrorDetail("Reading date time:" + actual + " does not conform to the defined interval:" + interval);

                    break;
                }
            }
        }

        return retVal;
    }

    private int getInterval() {
        Cache cache = cacheManager.getCache("config");

        int interval = DEFAULT_INTERVAL;
        String intervalObj = cache.get("MQ_INTERVAL", String.class);
        if (intervalObj != null) {
            interval = Integer.parseInt(intervalObj);
        }

        return interval;
    }

}

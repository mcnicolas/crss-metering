package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.service.CacheService;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static java.lang.Integer.parseInt;
import static java.util.Calendar.MINUTE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@Order(value = 5)
public class CorrectIntervalValidator implements Validator {

    private static final int DEFAULT_INTERVAL = 15;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    private final CacheService cacheService;

    @Autowired
    public CorrectIntervalValidator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        FileType fileType = fileManifest.getFileType();
        if (fileType == XLS || fileType == CSV) {
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
                    retVal.setErrorDetail("Reading date time:" + actual
                            + " does not conform to the defined interval:" + interval + " minutes");

                    break;
                }
            }
        }

        return retVal;
    }

    private int getInterval() {
        String interval = cacheService.getConfig("MQ_INTERVAL");

        return isNotBlank(interval) ? parseInt(interval) : DEFAULT_INTERVAL;
    }

}

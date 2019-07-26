package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.service.CacheService;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
@Order(value = 6)
public class CorrectIntervalValidator implements Validator {

    private static final int DEFAULT_INTERVAL = 5;
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

            Pair<ValidationResult, Long> validationResultLongPair = validateInterval(queue, interval);

            if (validationResultLongPair.getLeft().getStatus() == REJECTED) {
                if (interval == 5 && meterData.isConvertToFiveMin()) {
                    validationResultLongPair = validateInterval(new LinkedList<>(meterData.getDetails()), 15);
                }
            }

            if (validationResultLongPair.getLeft().getStatus() == REJECTED) {
                retVal.setStatus(REJECTED);
                if (meterData.isConvertToFiveMin()) {
                    if (interval == 15) {
                        retVal.setErrorDetail("Reading date time:" + validationResultLongPair.getRight()
                                + " does not conform to the available interval: " + interval + " minutes");
                    } else {
                        retVal.setErrorDetail("Reading date time:" + validationResultLongPair.getRight()
                                + " does not conform to the available intervals: 5 or 15 minutes");
                    }
                } else {
                    retVal.setErrorDetail("Reading date time:" + validationResultLongPair.getRight()
                            + " does not conform to the available interval: " + interval + " minutes");
                }
            }
        }

        return retVal;
    }

    private Pair<ValidationResult, Long> validateInterval(Queue<MeterDataDetail> queue, int interval) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setStatus(ACCEPTED);
        Pair<ValidationResult, Long> retVal = Pair.of(validationResult, null);

        long firstRecord = queue.poll().getReadingDateTime();

        Calendar expectedDateTime = Calendar.getInstance();
        try {
            expectedDateTime.setTime(DATE_FORMAT.parse(String.valueOf(firstRecord)));
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        while (queue.peek() != null) {
            expectedDateTime.add(MINUTE, interval);

            long expected = Long.parseLong(DATE_FORMAT.format(expectedDateTime.getTime()));
            long actual = queue.poll().getReadingDateTime();

            if (expected != actual) {
                validationResult.setStatus(REJECTED);
                retVal = Pair.of(validationResult, actual);
            }
        }

        return retVal;
    }

    private int getInterval() {
        String interval = cacheService.getConfig("MQ_INTERVAL");

        return isNotBlank(interval) ? parseInt(interval) : DEFAULT_INTERVAL;
    }

}

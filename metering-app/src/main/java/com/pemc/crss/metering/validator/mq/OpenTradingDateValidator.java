package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.UploadType;
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
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.DateTimeUtils.isWithinDays;
import static java.lang.Integer.parseInt;
import static java.util.Calendar.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@Order(value = 4)
public class OpenTradingDateValidator implements Validator {

    private static final int DEFAULT_GATE_CLOSURE = 1;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat READING_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    private final CacheService cacheService;

    @Autowired
    public OpenTradingDateValidator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);
        retVal = validateNullDate(meterData);
        if (retVal.getStatus() == ACCEPTED) {
            retVal = validateOpenTradingDate(fileManifest, meterData);
        }

        return retVal;
    }

    private ValidationResult validateNullDate(MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        List<MeterDataDetail> meterDataDetails = meterData.getDetails();

        for (MeterDataDetail meterDataDetail : meterDataDetails) {
            if (meterDataDetail.getReadingDateTime() == null) {
                retVal.setStatus(REJECTED);

                String errorMessage = "Malformed trading date format. Should be YYYY-MM-DD HH:MM";
                retVal.setErrorDetail(errorMessage);

                break;
            }
        }

        return retVal;
    }

    private ValidationResult validateOpenTradingDate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        UploadType uploadType = fileManifest.getUploadType();
        if (uploadType == DAILY) {
            Date now = new Date();

            List<MeterDataDetail> meterDataDetails = meterData.getDetails();

            for (MeterDataDetail meterDataDetail : meterDataDetails) {
                if (meterDataDetail.getReadingDateTime() == null) {
                    retVal.setStatus(REJECTED);

                    String errorMessage = "Malformed trading date format. Should be YYYY-MM-DD HH:MM";
                    retVal.setErrorDetail(errorMessage);

                    break;
                }

                Date readingDateTime = null;
                try {
                    readingDateTime = READING_DATE_FORMAT.parse(String.valueOf(meterDataDetail.getReadingDateTime()));
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }

                int gateClosure = getGateClosure();
                if (!isSameDay(now, readingDateTime) && !isWithinDays(now, readingDateTime, gateClosure)) {
                    retVal.setStatus(REJECTED);

                    String errorMessage = "Trading Date is Not Allowed. Submission of Daily MQ is closed for "
                            + dateFormat.format(readingDateTime);
                    retVal.setErrorDetail(errorMessage);

                    break;
                }
            }
        }

        return retVal;
    }

    // TODO: Quick hack. Need to refactor
    public boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }

        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    // TODO: Quick hack. Need to refactor
    public boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }

        if (cal1.get(ERA) == cal2.get(ERA) && cal1.get(YEAR) == cal2.get(YEAR)) {
            if (cal1.get(DAY_OF_YEAR) == cal2.get(DAY_OF_YEAR)) {
                return true;
            } else if ((cal1.get(DAY_OF_YEAR) + 1) == cal2.get(DAY_OF_YEAR)) {
                if (cal2.get(HOUR_OF_DAY) == 0 && cal2.get(MINUTE) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getGateClosure() {
        String interval = cacheService.getConfig("MQ_ALLOWABLE_TRADING_DATE");

        return isNotBlank(interval) ? parseInt(interval) : DEFAULT_GATE_CLOSURE;
    }

}

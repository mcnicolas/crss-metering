package com.pemc.crss.metering.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public final class DateTimeUtils {

    private static final DateFormat READING_DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    private static final DateFormat DATE_PARAM_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    private DateTimeUtils() {
    }

    public static Date parseDate(String date) {
        Date retVal = new Date();

        if (isNotBlank(date)) {
            try {
                retVal = DATE_PARAM_FORMAT.parse(date);
            } catch (ParseException e) {
                log.warn(e.getMessage(), e);
            }
        }

        return retVal;
    }

    public static Date startOfMonth(Date date) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTime(date);
        retVal.set(DAY_OF_MONTH, 1);
        retVal.set(HOUR_OF_DAY, 0);
        retVal.set(MINUTE, 0);
        retVal.set(SECOND, 0);

        return retVal.getTime();
    }

    public static Date endOfMonth(Date date) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTime(date);
        retVal.set(DAY_OF_MONTH, retVal.getActualMaximum(DAY_OF_MONTH));
        retVal.set(HOUR_OF_DAY, 23);
        retVal.set(MINUTE, 59);
        retVal.set(SECOND, 59);

        return retVal.getTime();
    }

    public static Date startOfDay(Date date) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTime(date);
        retVal.set(HOUR_OF_DAY, 0);
        retVal.set(MINUTE, 0);
        retVal.set(SECOND, 0);

        return retVal.getTime();
    }

    public static Date endOfDay(Date date) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTime(date);
        retVal.set(HOUR_OF_DAY, 23);
        retVal.set(MINUTE, 59);
        retVal.set(SECOND, 59);

        return retVal.getTime();
    }

    public static long dateToLong(Date date) {
        String formattedDate = READING_DATE_TIME_FORMAT.format(date);

        return Long.valueOf(formattedDate);
    }

    public static boolean isStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(HOUR_OF_DAY) == 0
                && calendar.get(MINUTE) == 0
                && calendar.get(SECOND) == 0;
    }

    public static boolean isYesterday(Date now, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(now);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        calendar1.add(DATE,-1);

        return calendar1.get(YEAR) == calendar2.get(YEAR)
                && calendar1.get(MONTH) == calendar2.get(MONTH)
                && calendar1.get(DATE) == calendar2.get(DATE);
    }

}

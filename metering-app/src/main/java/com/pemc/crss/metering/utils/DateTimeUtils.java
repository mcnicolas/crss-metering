package com.pemc.crss.metering.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public final class DateTimeUtils {

    public static final DateFormat DATE_PARAM_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter READING_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DTF_24HR = (new DateTimeFormatterBuilder()).appendPattern("MM/dd/yyyy HH:mm").toFormatter();


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
        retVal.set(MILLISECOND, 0);

        return retVal.getTime();
    }

    public static Date startOfDayMQ(Date date) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTime(date);
        retVal.set(HOUR_OF_DAY, 0);
        retVal.set(MINUTE, 1);
        retVal.set(SECOND, 0);
        retVal.set(MILLISECOND, 0);

        return retVal.getTime();
    }

    public static Date endOfDay(Date date) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTime(date);
        retVal.set(HOUR_OF_DAY, 23);
        retVal.set(MINUTE, 59);
        retVal.set(SECOND, 59);
        retVal.set(MILLISECOND, 999);

        return retVal.getTime();
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

    public static Long parseDateAsLong(String strDate, String time) throws com.pemc.crss.metering.parser.ParseException {
        try {
            LocalDateTime date = LocalDateTime.parse(strDate + " " + time, DATE_TIME_FORMATTER);

            return Long.parseLong(date.format(READING_DATETIME));
        } catch (DateTimeParseException e) {
            log.error(e.getMessage(), e);
            throw new com.pemc.crss.metering.parser.ParseException("Incorrect Date/Time Format.");
        }
    }

    public static String dateToString(Date date) {
        return DATE_PARAM_FORMAT.format(date);
    }

    public static Timestamp now() {
        return new Timestamp(Calendar.getInstance().getTime().getTime());
    }
    public static boolean isBetweenInclusive(LocalDateTime targetDate, LocalDateTime startDate, LocalDateTime endDate) {
        return targetDate != null && startDate != null && endDate != null?!targetDate.isBefore(startDate) && !targetDate.isAfter(endDate):false;
    }
    public static LocalDateTime parseDateTime24hr(String dateTime) {
        return StringUtils.isNotEmpty(dateTime)?LocalDateTime.parse(dateTime, DTF_24HR):null;
    }

}

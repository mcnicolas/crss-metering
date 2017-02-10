package com.pemc.crss.metering.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

import static org.apache.commons.lang3.time.DateFormatUtils.format;

public class BcqDateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_TIME_12_HR_FORMAT = "yyyy-MM-dd hh:mm a";
    private static final String LONG_DATE_FORMAT = "MMM. dd, yyyy";
    private static final String LONG_DATE_TIME_FORMAT = "MMM. dd, yyyy hh:mm a";

    private BcqDateUtils() {}

    public static Date parseDateTime(String dateString) {
        try {
            return DateUtils.parseDateStrictly(dateString, DATE_TIME_FORMAT);
        } catch (ParseException ignored) {}

        return null;
    }

    public static Date parseDate(String dateString) {
        try {
            return DateUtils.parseDateStrictly(dateString, DATE_FORMAT);
        } catch (ParseException ignored) {}

        return null;
    }

    public static String formatDate(Date date) {
        return format(date, DATE_FORMAT);
    }

    public static String formatDateTime(Date date) {
        return format(date, DATE_TIME_FORMAT);
    }

    public static String formatDateTime12Hr(Date date) {
        return format(date, DATE_TIME_12_HR_FORMAT);
    }

    public static String formatLongDate(Date date) {
        return format(date, LONG_DATE_FORMAT);
    }

    public static String formatLongDateTime(Date date) {
        return format(date, LONG_DATE_TIME_FORMAT);
    }

}

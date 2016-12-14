package com.pemc.crss.metering.parser.bcq.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class BcqDateUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
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
        return DateFormatUtils.format(date, DATE_FORMAT);
    }

    public static String formatDateTime(Date date) {
        return DateFormatUtils.format(date, DATE_TIME_FORMAT);
    }

    public static String formatLongDate(Date date) {
        return DateFormatUtils.format(date, LONG_DATE_FORMAT);
    }

    public static String formatLongDateTime(Date date) {
        return DateFormatUtils.format(date, LONG_DATE_TIME_FORMAT);
    }

}

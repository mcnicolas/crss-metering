package com.pemc.crss.metering.parser.bcq.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class BCQParserUtil {

    public static final String[] DATE_TIME_FORMATS = {
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm"
    };

    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",
            "yyyy/MM/dd"
    };

    private BCQParserUtil() {}

    public static Date parseDateTime(String dateString) {
        try {
            return DateUtils.parseDateStrictly(dateString, DATE_TIME_FORMATS);
        } catch (ParseException ignored) {}

        return null;
    }

    public static Date parseDate(String dateString) {
        try {
            return DateUtils.parseDateStrictly(dateString, DATE_FORMATS);
        } catch (ParseException ignored) {}

        return null;
    }

}

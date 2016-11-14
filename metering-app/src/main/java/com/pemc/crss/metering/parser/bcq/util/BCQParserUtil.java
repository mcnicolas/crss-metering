package com.pemc.crss.metering.parser.bcq.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class BCQParserUtil {

    public static final String[] DATE_TIME_FORMATS = {
            "MM/dd/yyyy HH:mm",
            "MM-dd-yyyy HH:mm"
    };

    private BCQParserUtil() {}

    public static Date parseDateTime(String dateString) {
        try {
            return DateUtils.parseDate(dateString, DATE_TIME_FORMATS);
        } catch (ParseException ignored) {}

        return null;
    }

}

package com.pemc.crss.metering.dto;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

@Data
public class MeterDataCSV {

    private String sein;
    private String readingDate;
    private String readingTime;
    private double kwd;
    private double kwhd;
    private double kvarhd;
    private double kwr;
    private double kwhr;
    private double kvarhr;

    public Date getReadingDateTime() {
        Calendar readingDateTime = null;

        String[] formats = {
                "MM/dd/yyyy",
                "dd/MM/yy"
        };

        Date date = null;
        for (String formatString : formats) {
            try {
                date = new SimpleDateFormat(formatString).parse(getReadingDate());
            } catch (ParseException e) {
            }
        }

        if (date != null) {
            readingDateTime = Calendar.getInstance();
            readingDateTime.setTime(date);

            String[] value = readingTime.split(":");

            readingDateTime.set(HOUR_OF_DAY, Integer.parseInt(value[0]));
            readingDateTime.set(MINUTE, Integer.parseInt(value[1]));

            return readingDateTime.getTime();
        }

        return null;
    }

}

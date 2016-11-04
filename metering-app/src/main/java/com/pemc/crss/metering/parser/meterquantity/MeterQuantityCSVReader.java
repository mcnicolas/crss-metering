package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.MeterData2;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
public class MeterQuantityCSVReader implements QuantityReader<MeterData2> {

    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    @Override
    public List<MeterData2> readData(InputStream inputStream) throws IOException {
        List<MeterData2> meterData = new ArrayList<>();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            reader.getHeader(true);

            List<String> row;
            while ((row = reader.read()) != null) {
                meterData.add(populateBean(row));
            }
        }

        return meterData;
    }

    private MeterData2 populateBean(List<String> row) {
        MeterData2 retVal = new MeterData2();

        retVal.setSein(row.get(0));
        retVal.setReadingDateTime(parseDateTime(row.get(1), row.get(2)));
        retVal.setKwd(getNumericValue(row.get(3)));
        retVal.setKwhd(getNumericValue(row.get(4)));
        retVal.setKvarhd(getNumericValue(row.get(5)));
        retVal.setKwr(getNumericValue(row.get(6)));
        retVal.setKwhr(getNumericValue(row.get(7)));
        retVal.setKvarhr(getNumericValue(row.get(8)));
        retVal.setEstimationFlag(row.get(9));

        return retVal;
    }

    private Double getNumericValue(String data) {
        if (NumberUtils.isParsable(data)) {
            return Double.parseDouble(data);
        } else {
            return null;
        }
    }

    private Date parseDateTime(String date, String time) {
        Date retVal = null;

        try {
            retVal = dateFormat.parse(date + " " + time);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

}

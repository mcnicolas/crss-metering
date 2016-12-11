package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.pemc.crss.metering.utils.DateTimeUtils.parseDateAsLong;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
public class MeterQuantityCSVReader implements QuantityReader {

    @Override
    public MeterData readData(InputStream inputStream) throws IOException {
        MeterData meterData = new MeterData();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<MeterDataDetail> meterDataDetails = new ArrayList<>();

            MeterDataHeader header = readHeader(reader.getHeader(true));

            List<String> row;
            while ((row = reader.read()) != null) {
                if (row.size() == 1) {
                    throw new IOException("Cannot parse CSV file. It might be an invalid CSV file or malformed.");
                }

                meterDataDetails.add(populateBean(row));
            }

            meterData.setHeader(header);
            meterData.setDetails(meterDataDetails);
        }

        return meterData;
    }

    private MeterDataHeader readHeader(String[] header) {
        MeterDataHeader meterDataHeader = new MeterDataHeader();
        meterDataHeader.setColumnNames(Arrays.asList(header));

        return meterDataHeader;
    }

    private MeterDataDetail populateBean(List<String> row) {
        MeterDataDetail retVal = new MeterDataDetail();

        retVal.setSein(row.get(0));
        retVal.setReadingDateTime(parseDateAsLong(row.get(1), row.get(2)));
        retVal.setKwd(getNumericValue(row.get(3)));
        retVal.setKwhd(getNumericValue(row.get(4)));
        retVal.setKvarhd(getNumericValue(row.get(5)));
        retVal.setKwr(getNumericValue(row.get(6)));
        retVal.setKwhr(getNumericValue(row.get(7)));
        retVal.setKvarhr(getNumericValue(row.get(8)));

        if (row.size() > 9) {
            retVal.setEstimationFlag(row.get(9));
        }

        return retVal;
    }

    private Double getNumericValue(String data) {
        if (NumberUtils.isParsable(data)) {
            return Double.parseDouble(data);
        } else {
            return null;
        }
    }

}

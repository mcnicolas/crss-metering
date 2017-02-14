package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.utils.DateTimeUtils.parseDateAsLong;
import static java.math.RoundingMode.HALF_UP;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
public class MeterQuantityCSVReader implements QuantityReader {

    @Override
    public MeterData readData(FileManifest fileManifest, InputStream inputStream) throws ParseException {
        MeterData meterData = new MeterData();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<MeterDataDetail> meterDataDetails = new ArrayList<>();

            MeterDataHeader header = readHeader(reader.getHeader(true));

            List<String> row;
            while ((row = reader.read()) != null) {
                if (row.size() == 1) {
                    throw new ParseException("Cannot parse CSV file. It might be an invalid CSV file or malformed.");
                }

                if (isBlank(row)) {
                    continue;
                }

                MeterDataDetail detail = populateBean(row);
                detail.setFileID(fileManifest.getFileID());
                detail.setUploadType(fileManifest.getUploadType());
                detail.setMspShortName(fileManifest.getMspShortName());
                detail.setCreatedDateTime(fileManifest.getUploadDateTime());

                meterDataDetails.add(detail);
            }

            meterData.setHeader(header);
            meterData.setDetails(meterDataDetails);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), e);
        }

        return meterData;
    }

    private boolean isBlank(List<String> row) {
        boolean retVal = false;

        if (StringUtils.isBlank(row.get(0))) {
            retVal = true;
        }

        return retVal;
    }

    private MeterDataHeader readHeader(String[] header) {
        MeterDataHeader meterDataHeader = new MeterDataHeader();
        meterDataHeader.setColumnNames(Arrays.asList(header));

        return meterDataHeader;
    }

    private MeterDataDetail populateBean(List<String> row) throws ParseException {
        MeterDataDetail retVal = new MeterDataDetail();

        retVal.setSein(row.get(0));
        retVal.setReadingDateTime(parseDateAsLong(row.get(1), row.get(2)));
        retVal.setKwd(getNumericValue(row, 3));
        retVal.setKwhd(getNumericValue(row, 4));
        retVal.setKvarhd(getNumericValue(row, 5));
        retVal.setKwr(getNumericValue(row, 6));
        retVal.setKwhr(getNumericValue(row, 7));
        retVal.setKvarhr(getNumericValue(row, 8));

        if (row.size() > 9) {
            retVal.setEstimationFlag(row.get(9));
        }

        return retVal;
    }

    private BigDecimal getNumericValue(List<String> row, int index) throws ParseException {
        BigDecimal retVal = null;

        if (row.size() > index) {
            String value = row.get(index);
            if (isParsable(value)) {
                retVal = new BigDecimal(value).setScale(17, HALF_UP);
            } else {
                throw new ParseException("Meter reading is not a number.");
            }
        }

        return retVal;
    }

}
